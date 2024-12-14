import dotenv from 'dotenv';
import { WebSocketServer, WebSocket } from 'ws';

dotenv.config();

// eslint-disable-next-line no-undef
const PORT = process.env.PORT || 3000;
const server = new WebSocketServer({ port: PORT });

// Generate a random 3-digit room code for chess games
const generateRoomCode = () => Math.floor(100 + Math.random() * 900).toString();

// Store chess rooms and collaborative templates separately
const chessRooms = {};
const rooms = {};
const chatHistory = {};
// Store persistent player roles for each room
const playerRoles = {}; // Structure: { roomId: { username: role, ... } }

server.on('connection', (ws) => {
  // eslint-disable-next-line no-console
  console.log('Server:-> New client connected.');

  let currentRoom = null;

  ws.on('message', (message) => {
    const data = JSON.parse(message);
    const { type, roomId, payload } = data;

    if (type === 'join') {
      if (!rooms[roomId]) {
        rooms[roomId] = new Set();
        chatHistory[roomId] = [];
      }
      rooms[roomId].add(ws);
      currentRoom = roomId;

      ws.send(
        JSON.stringify({ type: 'chat-history', payload: chatHistory[roomId] })
      );
    } else if (type === 'chess-game') {
      handleChessGame(ws, roomId, payload);
    } else if (type === 'update' && currentRoom) {
      broadcastToRoom(currentRoom, { type: 'refresh', payload });
    } else if (type === 'chat' && currentRoom) {
      const chatMessage = {
        user: payload.user,
        text: payload.text,
        timestamp: new Date(),
      };
      chatHistory[currentRoom].push(chatMessage);
      broadcastToRoom(currentRoom, { type: 'chat', payload: chatMessage });
    } else {
      // eslint-disable-next-line no-console
      console.log(`Server:-> Unhandled message type: ${type}`);
    }
  });

  ws.on('close', () => {
    if (currentRoom && rooms[currentRoom]) {
      rooms[currentRoom].delete(ws);
      if (rooms[currentRoom].size === 0) {
        delete rooms[currentRoom];
        delete chatHistory[currentRoom];
      }
    }
  });
});

function broadcastToRoom(roomId, message) {
  if (rooms[roomId]) {
    rooms[roomId].forEach((client) => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(JSON.stringify(message));
      }
    });
  }
}

function broadcastChess(roomId, message) {
  const room = chessRooms[roomId];

  if (!room) {
    // eslint-disable-next-line no-console
    console.error(`Server:-> Chess room ${roomId} does not exist.`);
    return;
  }

  // Send to all players
  room.players.forEach((player) => {
    if (player.ws.readyState === WebSocket.OPEN) {
      player.ws.send(JSON.stringify(message));
    } else {
      // eslint-disable-next-line no-console
      console.warn(`Server:-> Player WebSocket not open: ${player.username}`);
    }
  });

  // Send to all spectators
  room.spectators.forEach((spectator) => {
    if (spectator.readyState === WebSocket.OPEN) {
      spectator.send(JSON.stringify(message));
    } else {
      // eslint-disable-next-line no-console
      console.warn(`Server:-> Spectator WebSocket not open`);
    }
  });
}

function handleChessGame(ws, roomId, payload) {
  const { action, username, moveData } = payload;

  if (action === 'join-chess') {
    if (roomId === 'new') {
      const newRoomId = generateRoomCode();
      chessRooms[newRoomId] = {
        players: [],
        spectators: [],
        fen: initialFen(),
      };

      addPlayerToRoom(ws, newRoomId, username);
    } else {
      if (!chessRooms[roomId]) {
        ws.send(
          JSON.stringify({ type: 'error', roomId, payload: 'Room not found.' })
        );
        return;
      }
      addPlayerToRoom(ws, roomId, username);
    }
  } else if (action === 'move') {
    handleMove(ws, roomId, moveData);
  } else if (action === 'chat') {
    // Handle chat messages in chess game rooms
    const chatMessage = {
      user: username,
      text: payload.text,
      timestamp: new Date(),
    };
    // Add chat message to history
    if (!chatHistory[roomId]) chatHistory[roomId] = [];
    chatHistory[roomId].push(chatMessage);

    // Broadcast chat message to room
    broadcastChess(roomId, {
      type: 'chess-game',
      roomId,
      payload: {
        action: 'chat',
        chatMessage,
      },
    });
  }
}

function handleMove(ws, roomId, moveData) {
  const room = chessRooms[roomId];

  if (!room) {
    ws.send(JSON.stringify({ type: 'error', payload: 'Room not found.' }));
    return;
  }

  if (!moveData) {
    ws.send(
      JSON.stringify({ type: 'error', payload: 'Move data is missing.' })
    );
    return;
  }

  const { from, to, fen, promotion } = moveData;

  // Update the room's FEN
  room.fen = fen;

  // Broadcast the move to all players and spectators
  broadcastChess(roomId, {
    type: 'chess-game',
    roomId,
    payload: {
      action: 'move',
      moveData: { from, to, fen, promotion },
    },
  });
}

function addPlayerToRoom(ws, roomId, username) {
  const room = chessRooms[roomId];
  let role;

  // Initialize roles for the room if not already done
  if (!playerRoles[roomId]) {
    playerRoles[roomId] = {};
  }

  // Check if the player already has a role assigned
  if (playerRoles[roomId][username]) {
    role = playerRoles[roomId][username];
  } else if (room.players.length < 2) {
    // Randomly assign black or white to the first two players
    if (room.players.length === 0) {
      role = Math.random() < 0.5 ? 'White' : 'Black';
    } else {
      role = room.players[0].role === 'White' ? 'Black' : 'White';
    }

    room.players.push({ username, ws, role });
    playerRoles[roomId][username] = role; // Persist the role
  } else {
    // Assign spectators
    role = 'Spectator';
    room.spectators.push(ws);
  }
  // Prepare the current state of the room
  const roomState = {
    fen: room.fen, // Include the current FEN
    white:
      room.players.find((player) => player.role === 'White')?.username ||
      'Waiting for White...',
    black:
      room.players.find((player) => player.role === 'Black')?.username ||
      'Waiting for Black...',
    spectators: room.spectators.map(() => 'Spectator'), // Spectators don't need usernames
  };

  // Include chat history in the state
  const chatHistoryForRoom = chatHistory[roomId] || [];

  // Send the current room state, including the FEN and chat history, to the joining client
  ws.send(
    JSON.stringify({
      type: 'chess-game',
      roomId,
      payload: {
        action: 'joined',
        username,
        role,
        roomId,
        state: roomState,
        chatHistory: chatHistoryForRoom,
      },
    })
  );

  // Broadcast the updated room state to all other clients
  broadcastChess(roomId, {
    type: 'chess-game',
    roomId,
    payload: {
      action: 'room-update',
      state: roomState, // Include the FEN in the room update as well
    },
  });
}

function initialFen() {
  return 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1'; // Standard starting FEN
}
