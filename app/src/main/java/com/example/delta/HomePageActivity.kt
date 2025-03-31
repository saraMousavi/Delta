package com.example.delta

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.delta.data.entity.Buildings

class HomePageActivity : ComponentActivity() {
    private val viewModel: BuildingsViewModel by viewModels()


    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            AppTheme {
                DetailDrawer(
                    title = getString(R.string.menu_title),
                    imageId = R.drawable.profilepic
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // List of buildings
                        BuildingList(viewModel = viewModel)
                        val context = LocalContext.current
                        // FAB (fixed alignment)
                        FloatingActionButton(
                            onClick = { context.startActivity(Intent(context, BuildingFormActivity::class.java)) },
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.BottomEnd),
                            containerColor = Color(context.getColor(R.color.secondary_color))
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add Building")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BuildingList(viewModel: BuildingsViewModel) {
    val buildings by viewModel.getAllBuildings().collectAsState(initial = emptyList())
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp)
    ) {

        items(buildings) { building ->
            BuildingCard(building = building){
                val intent = Intent(context, BuildingProfileActivity::class.java).apply {
                    putExtra("BUILDING_DATA", building as Parcelable)
                }
                context.startActivity(intent)
            }
        }
    }
}

@Composable
fun BuildingCard(building: Buildings, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
        .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp) // Card shape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Image with matching corner radius
            Image(
                painter = painterResource(id = R.drawable.building_image),
                contentDescription = "Building Image",
                modifier = Modifier
                    .fillMaxWidth() // Match card width
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)) // Top corners only
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = building.name,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = building.ownerName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
