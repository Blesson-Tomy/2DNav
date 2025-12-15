package com.example.a2dnav

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    private lateinit var buildingSpinner: Spinner
    private lateinit var loadBuildingsButton: Button
    private lateinit var fetchWallsButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var statusTextView: TextView
    private lateinit var wallsTextView: TextView

    private val db = FirebaseFirestore.getInstance()
    private val TAG = "MainActivity"

    private var buildingsList = mutableListOf<BuildingItem>()
    private var selectedBuilding: BuildingItem? = null

    data class BuildingItem(
        val id: String,
        val name: String,
        val campus: String = ""
    ) {
        override fun toString(): String {
            return if (campus.isNotEmpty()) "$name ($campus)" else name
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        buildingSpinner = findViewById(R.id.buildingSpinner)
        loadBuildingsButton = findViewById(R.id.loadBuildingsButton)
        fetchWallsButton = findViewById(R.id.fetchWallsButton)
        progressBar = findViewById(R.id.progressBar)
        statusTextView = findViewById(R.id.statusTextView)
        wallsTextView = findViewById(R.id.wallsTextView)

        // Initial state
        fetchWallsButton.isEnabled = false
        statusTextView.text = "Click 'Load Buildings' to fetch available buildings from Firebase"

        // Load buildings button
        loadBuildingsButton.setOnClickListener {
            loadAvailableBuildings()
        }

        // Fetch walls button
        fetchWallsButton.setOnClickListener {
            selectedBuilding?.let { building ->
                fetchWallsForBuilding(building)
            }
        }

        // Spinner selection listener
        buildingSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (buildingsList.isNotEmpty()) {
                    selectedBuilding = buildingsList[position]
                    statusTextView.text = "Selected: ${selectedBuilding?.name}\nClick 'Fetch Walls' to load wall data"
                    fetchWallsButton.isEnabled = true
                    wallsTextView.text = "" // Clear previous walls
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedBuilding = null
                fetchWallsButton.isEnabled = false
            }
        }
    }

    private fun loadAvailableBuildings() {
        loadBuildingsButton.isEnabled = false
        fetchWallsButton.isEnabled = false
        progressBar.visibility = View.VISIBLE
        statusTextView.text = "Loading buildings from Firebase..."
        wallsTextView.text = ""

        lifecycleScope.launch {
            try {
                Log.d(TAG, "Fetching buildings from Firebase...")

                // Fetch all buildings from Firebase
                val buildingsSnapshot = db.collection("buildings")
                    .get()
                    .await()

                if (buildingsSnapshot.isEmpty) {
                    // No buildings found
                    statusTextView.text = "❌ No buildings found in database"
                    Toast.makeText(
                        this@MainActivity,
                        "No buildings found",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.w(TAG, "No buildings found in database")
                    return@launch
                }

                // Parse buildings
                buildingsList.clear()
                buildingsSnapshot.documents.forEach { doc ->
                    val id = doc.id
                    val name = doc.getString("name") ?: "Unknown Building"
                    val campus = doc.getString("campus") ?: ""

                    buildingsList.add(BuildingItem(id, name, campus))
                    Log.d(TAG, "Found building: $name (ID: $id)")
                }

                // Update spinner
                val adapter = ArrayAdapter(
                    this@MainActivity,
                    android.R.layout.simple_spinner_item,
                    buildingsList
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                buildingSpinner.adapter = adapter

                // Update status
                statusTextView.text = "✅ Found ${buildingsList.size} building(s)\nSelect a building from dropdown"

                Toast.makeText(
                    this@MainActivity,
                    "✅ Loaded ${buildingsList.size} buildings",
                    Toast.LENGTH_SHORT
                ).show()

                Log.d(TAG, "Successfully loaded ${buildingsList.size} buildings")

            } catch (e: Exception) {
                // Error occurred
                statusTextView.text = "❌ Error loading buildings: ${e.message}"
                Toast.makeText(
                    this@MainActivity,
                    "❌ Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e(TAG, "Error loading buildings", e)

            } finally {
                progressBar.visibility = View.GONE
                loadBuildingsButton.isEnabled = true
            }
        }
    }

    private fun fetchWallsForBuilding(building: BuildingItem) {
        fetchWallsButton.isEnabled = false
        progressBar.visibility = View.VISIBLE
        statusTextView.text = "Fetching walls for ${building.name}..."
        wallsTextView.text = ""

        lifecycleScope.launch {
            try {
                Log.d(TAG, "Fetching walls for building: ${building.id}")

                // Fetch walls for the selected building
                // Path: buildings/{buildingId}/floors/floor1/walls
                val wallsSnapshot = db.collection("buildings")
                    .document(building.id)
                    .collection("floors")
                    .document("floor1")  // Default to floor1
                    .collection("walls")
                    .get()
                    .await()

                if (wallsSnapshot.isEmpty) {
                    // No walls found
                    statusTextView.text = "⚠️ No walls found for ${building.name}"
                    wallsTextView.text = "This building has no wall data in the database.\n\nPath checked:\nbuildings/${building.id}/floors/floor1/walls"
                    Toast.makeText(
                        this@MainActivity,
                        "⚠️ No walls found",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.w(TAG, "No walls found for building: ${building.id}")
                    return@launch
                }

                // Display walls
                val totalWalls = wallsSnapshot.size()
                statusTextView.text = "✅ Found $totalWalls walls for ${building.name}"

                // Build walls text
                val wallsText = StringBuilder()
                wallsText.append("Total Walls: $totalWalls\n")
                wallsText.append("Building: ${building.name}\n")
                wallsText.append("Building ID: ${building.id}\n")
                wallsText.append("\n" + "=".repeat(40) + "\n\n")

                // Show first 20 walls (to avoid overwhelming the screen)
                val wallsToShow = minOf(20, totalWalls)
                wallsSnapshot.documents.take(wallsToShow).forEachIndexed { index, doc ->
                    val x1 = doc.getLong("x1") ?: 0
                    val y1 = doc.getLong("y1") ?: 0
                    val x2 = doc.getLong("x2") ?: 0
                    val y2 = doc.getLong("y2") ?: 0

                    wallsText.append("Wall ${index + 1}:\n")
                    wallsText.append("  From: ($x1, $y1)\n")
                    wallsText.append("  To:   ($x2, $y2)\n")
                    wallsText.append("\n")
                }

                if (totalWalls > wallsToShow) {
                    wallsText.append("\n... and ${totalWalls - wallsToShow} more walls\n")
                    wallsText.append("\n(Showing first $wallsToShow of $totalWalls walls)")
                }

                wallsTextView.text = wallsText.toString()

                Toast.makeText(
                    this@MainActivity,
                    "✅ Loaded $totalWalls walls",
                    Toast.LENGTH_SHORT
                ).show()

                Log.d(TAG, "Successfully fetched $totalWalls walls for ${building.id}")

            } catch (e: Exception) {
                // Error occurred
                statusTextView.text = "❌ Error fetching walls: ${e.message}"
                wallsTextView.text = "Error details:\n${e.message}\n\nStack trace:\n${e.stackTraceToString()}"
                Toast.makeText(
                    this@MainActivity,
                    "❌ Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e(TAG, "Error fetching walls", e)

            } finally {
                progressBar.visibility = View.GONE
                fetchWallsButton.isEnabled = true
            }
        }
    }
}