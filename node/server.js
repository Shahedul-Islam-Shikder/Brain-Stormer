import dotenv from "dotenv";
import { WebSocketServer, WebSocket } from "ws";

dotenv.config();

const PORT = process.env.PORT || 3000;
const server = new WebSocketServer({ port: PORT });

console.log(`Server:-> Collaborative WebSocket server running on ws://localhost:${PORT}`);

// Generate a random 3-digit room code for chess games
const generateRoomCode = () => Math.floor(100 + Math.random() * 900).toString();

// Store chess rooms and collaborative templates separately
const chessRooms = {};
const rooms = {};
const chatHistory = {};

server.on("connection", (ws) => {
  console.log("Server:-> New client connected.");

  let currentRoom = null;

  ws.on("message", (message) => {
    const data = JSON.parse(message);
    const { type, roomId, payload } = data;
    console.log(`Server:-> Message received. Type: ${type}, RoomID: ${roomId}`);

    console.log(`Server:-> Message received. Type: ${type}, RoomID: ${roomId}`);

    if (type === "join") {
    console.log(`Server:-> Joining room: ${roomId}`);
    }
      if (!rooms[roomId]) {
        rooms[roomId] = new Set();
        chatHistory[roomId] = [];
      }
      rooms[roomId].add(ws);
      currentRoom = roomId;

      console.log(`Server:-> Client joined room: ${roomId}`);

      ws.send(
        JSON.stringify({ type: "chat-history", payload: chatHistory[roomId] })
      );
    } else if (type === "chess-game") {
      console.log(`Server:-> Handling chess game for RoomID: ${roomId}`);
      handleChessGame(ws, roomId, payload);
    } else if (type === "update" && currentRoom) {
      console.log(`Server:-> Update received for room: ${currentRoom}`);
      broadcastToRoom(currentRoom, { type: "refresh", payload });
    } else if (type === "chat" && currentRoom) {
      const chatMessage = { user: payload.user, text: payload.text, timestamp: new Date() };
      chatHistory[currentRoom].push(chatMessage);
      broadcastToRoom(currentRoom, { type: "chat", payload: chatMessage });
    } else {
      console.log(`Server:-> Unhandled message type: ${type}`);
    }
  });

  ws.on("close", () => {
    if (currentRoom && rooms[currentRoom]) {
      rooms[currentRoom].delete(ws);
      if (rooms[currentRoom].size === 0) {
        delete rooms[currentRoom];
        delete chatHistory[currentRoom];
      }
      console.log(`Server:-> Client disconnected from room: ${currentRoom}`);
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
    console.error(`Server:-> Chess room ${roomId} does not exist.`);
    return;
  }

  console.log(`Server:-> Broadcasting chess message to room: ${roomId}`);
  console.log(`Server:-> Message: ${JSON.stringify(message)}`);

  // Send to all players
  room.players.forEach((player) => {
    if (player.ws.readyState === WebSocket.OPEN) {
      player.ws.send(JSON.stringify(message));
    } else {
      console.warn(`Server:-> Player WebSocket not open: ${player.username}`);
    }
  });

  // Send to all spectators
  room.spectators.forEach((spectator) => {
    if (spectator.readyState === WebSocket.OPEN) {
      spectator.send(JSON.stringify(message));
    } else {
      console.warn(`Server:-> Spectator WebSocket not open`);
    }
  });
}


function handleChessGame(ws, roomId, payload) {
  const { action, username, moveData } = payload;

  if (action === "join-chess") {
    if (roomId === "new") {
      const newRoomId = generateRoomCode();
      chessRooms[newRoomId] = { players: [], spectators: [], fen: initialFen() };
      console.log(`Server:-> New chess room created: ${newRoomId}`);
      addPlayerToRoom(ws, newRoomId, username);
    } else {
      if (!chessRooms[roomId]) {
        ws.send(JSON.stringify({ type: "error", roomId, payload: "Room not found." }));
        return;
      }
      addPlayerToRoom(ws, roomId, username);
    }
  } else if (action === "move") {
    handleMove(ws, roomId, moveData);
  }
}

function handleMove(ws, roomId, moveData) {
  const room = chessRooms[roomId];

  if (!room) {
    ws.send(JSON.stringify({ type: "error", payload: "Room not found." }));
    return;
  }

  if (!moveData) {
      ws.send(JSON.stringify({ type: "error", payload: "Move data is missing." }));
      return;
  }


  const { from, to, fen, promotion } = moveData;

  console.log(`Server:-> Move received: ${from} -> ${to}, FEN: ${fen}`);

  // Update the room's FEN
  room.fen = fen;

  // Broadcast the move to all players and spectators
  broadcastChess(roomId, {
    type: "chess-game",
    roomId,
    payload: {
      action: "move",
      moveData: { from, to, fen, promotion },
    },
  });
}

function addPlayerToRoom(ws, roomId, username) {
  const room = chessRooms[roomId];
  let role;

  // Determine the player's role
  if (room.players.length < 2) {
    role = room.players.length === 0 ? "White" : "Black";
    room.players.push({ username, ws, role });
  } else {
    role = "Spectator";
    room.spectators.push(ws);
  }

  console.log(`Server:-> User ${username} joined room ${roomId} as ${role}`);

  // Prepare the current state of the room
  const roomState = {
    fen: room.fen, // Include the current FEN
    white: room.players.find((player) => player.role === "White")?.username || "Waiting for White...",
    black: room.players.find((player) => player.role === "Black")?.username || "Waiting for Black...",
    spectators: room.spectators.map(() => "Spectator"), // Spectators don't need usernames
  };

  // Send the current room state, including the FEN, to the joining client
  ws.send(
    JSON.stringify({
      type: "chess-game",
      roomId,
      payload: {
        action: "joined",
        username,
        role,
        roomId,
        state: roomState, // Pass the room state with the FEN
      },
    })
  );

  // Broadcast the updated room state to all other clients
  broadcastChess(roomId, {
    type: "chess-game",
    roomId,
    payload: {
      action: "room-update",
      state: roomState, // Include the FEN in the room update as well
    },
  });
}


function initialFen() {
  return "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"; // Standard starting FEN
}




