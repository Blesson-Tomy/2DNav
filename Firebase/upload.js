const admin = require('firebase-admin');
const serviceAccount = require('./google-services.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

const entrances=[
    {
      "id": 1,
      "x": 881.0,
      "y": 1776.0,
      "available": true,
      "name": null,
      "room_no": null,
      "stairs": true
    },
    {
      "id": 2,
      "x": 881.0,
      "y": 1504.0,
      "available": true,
      "name": null,
      "room_no": null,
      "stairs": true
    },
    {
      "id": 3,
      "x": 741.5,
      "y": 1504.0,
      "available": true,
      "name": null,
      "room_no": null,
      "stairs": true
    },
    {
      "id": 4,
      "x": 667.0,
      "y": 1376.0,
      "available": true,
      "name": "MTB Department Library",
      "room_no": "201"
    },
    {
      "id": 5,
      "x": 1385.0,
      "y": 649.0,
      "available": true,
      "name": "Department Library Electrical Engg",
      "room_no": "214"
    },
    {
      "id": 6,
      "x": 1786.0,
      "y": 870.5,
      "available": true,
      "name": null,
      "room_no": null,
      "stairs": true
    },
    {
      "id": 7,
      "x": 1512.0,
      "y": 870.5,
      "available": true,
      "name": null,
      "room_no": null,
      "stairs": true
    },
    {
      "id": 8,
      "x": 1512.0,
      "y": 727.5,
      "available": true,
      "name": null,
      "room_no": null,
      "stairs": true
    }
  ]


// async function uploadFloorData() {
//   // Store all walls and entrances in a single document
//   await db.doc('buildings/MTB/floors/floor1').set({
//     walls: walls,
//     entrances: entrances,
//     floorName: 'Floor 1',
//     buildingId: 'MTB',
//     totalWalls: walls.length,
//     totalEntrances: entrances.length,
//     updatedAt: admin.firestore.FieldValue.serverTimestamp()
//   });

async function updatePolygonData() {
  await db.doc('buildings/MTB/floors/floor1.5').update({
    entrances: entrances,
    totalEntrances: entrances.length,
    updatedAt: admin.firestore.FieldValue.serverTimestamp()
  });

  
  console.log(`✓ Successfully uploaded ${entrances.length} entrances to floor1 document!`);
}

updatePolygonData().catch(console.error);