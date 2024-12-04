import dotenv from 'dotenv';
import { WebSocketServer, WebSocket } from 'ws'; // Import WebSocket here

dotenv.config();

const PORT = process.env.PORT || 3000;
const server = new WebSocketServer({ port: PORT });

console.log(`Collaborative WebSocket server running on ws://localhost:${PORT}`);

const rooms = {}; // Store clients by roomId

// Handle new connections
server.on('connection', (ws) => {
  console.log('New client connected.');

  let currentRoom = null;

  // Handle incoming messages
  ws.on('message', (message) => {
    const data = JSON.parse(message);
    const { type, roomId, payload } = data;

    // Join a room
    if (type === 'join') {
      if (!rooms[roomId]) {
        rooms[roomId] = new Set();
      }
      rooms[roomId].add(ws);
      currentRoom = roomId;
      console.log(`Client joined room: ${roomId}`);
    }

    // Notify all clients in the room to refresh
    if (type === 'update' && currentRoom) {
      console.log(`Update received for room: ${currentRoom}`);
      broadcastToRoom(currentRoom, { type: 'refresh', payload });
    }
  });

  // Handle disconnections
  ws.on('close', () => {
    if (currentRoom && rooms[currentRoom]) {
      rooms[currentRoom].delete(ws);
      if (rooms[currentRoom].size === 0) {
        delete rooms[currentRoom];
      }
      console.log(`Client disconnected from room: ${currentRoom}`);
    }
  });
});

// Broadcast message to all clients in a room
function broadcastToRoom(roomId, message) {
  if (rooms[roomId]) {
    rooms[roomId].forEach((client) => {
      if (client.readyState === WebSocket.OPEN) { // Correct WebSocket reference
        client.send(JSON.stringify(message));
      }
    });
  }
}
