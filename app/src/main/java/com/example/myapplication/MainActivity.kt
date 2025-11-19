@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

data class Person(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val birthDate: String,
    val phone: String,
    val email: String,
    val address: String
)

class PersonDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "persons.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE persons (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                first_name TEXT,
                last_name TEXT,
                birth_date TEXT,
                phone TEXT,
                email TEXT,
                address TEXT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS persons")
        onCreate(db)
    }

    fun insertPerson(person: Person): Long {
        val values = ContentValues().apply {
            put("first_name", person.firstName)
            put("last_name", person.lastName)
            put("birth_date", person.birthDate)
            put("phone", person.phone)
            put("email", person.email)
            put("address", person.address)
        }
        return writableDatabase.insert("persons", null, values)
    }

    fun getAllPersons(): List<Person> {
        val list = mutableListOf<Person>()
        val cursor = readableDatabase.query(
            "persons",
            null,
            null,
            null,
            null,
            null,
            "last_name ASC"
        )

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val person = Person(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        firstName = it.getString(it.getColumnIndexOrThrow("first_name")),
                        lastName = it.getString(it.getColumnIndexOrThrow("last_name")),
                        birthDate = it.getString(it.getColumnIndexOrThrow("birth_date")),
                        phone = it.getString(it.getColumnIndexOrThrow("phone")),
                        email = it.getString(it.getColumnIndexOrThrow("email")),
                        address = it.getString(it.getColumnIndexOrThrow("address"))
                    )
                    list.add(person)
                } while (it.moveToNext())
            }
        }
        return list
    }

    fun deletePerson(id: Long) {
        writableDatabase.delete("persons", "id = ?", arrayOf(id.toString()))
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PersonApp()
        }
    }
}

enum class Screen(val title: String) {
    HOME("Ekran główny"),
    ADD("Dodaj osobę"),
    LIST("Lista osób"),
    DELETE("Usuń osobę")
}

@Composable
fun PersonApp() {
    val context = LocalContext.current
    val db = remember { PersonDatabaseHelper(context) }

    val persons = remember {
        mutableStateListOf<Person>().apply {
            addAll(db.getAllPersons())
        }
    }

    var currentScreen by remember { mutableStateOf(Screen.HOME) }

    BackHandler(enabled = currentScreen != Screen.HOME) {
        currentScreen = Screen.HOME
    }

    val onSavePerson: (String, String, String, String, String, String) -> Unit =
        { f, l, b, p, e, a ->
            val tempPerson = Person(0, f, l, b, p, e, a)
            val newId = db.insertPerson(tempPerson)
            if (newId != -1L) persons.add(tempPerson.copy(id = newId))
        }

    val onDeletePerson: (Person) -> Unit = { person ->
        db.deletePerson(person.id)
        persons.remove(person)
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentScreen.title) },
                    navigationIcon = {
                        if (currentScreen != Screen.HOME) {
                            IconButton(onClick = { currentScreen = Screen.HOME }) {
                                Text("<")
                            }
                        }
                    }
                )
            },
            bottomBar = {
                BottomTabBar(
                    currentScreen = currentScreen,
                    onNavigate = { currentScreen = it }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (currentScreen) {
                    Screen.HOME -> HomeScreen { currentScreen = it }
                    Screen.ADD -> AddPersonScreen(onSavePerson)
                    Screen.LIST -> ListPersonsScreen(persons)
                    Screen.DELETE -> DeletePersonScreen(persons, onDeletePerson)
                }
            }
        }
    }
}

@Composable
fun BottomTabBar(currentScreen: Screen, onNavigate: (Screen) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = currentScreen == Screen.ADD,
            onClick = { onNavigate(Screen.ADD) },
            icon = { Text("A") },
            label = { Text("Dodaj") }
        )
        NavigationBarItem(
            selected = currentScreen == Screen.LIST,
            onClick = { onNavigate(Screen.LIST) },
            icon = { Text("L") },
            label = { Text("Lista") }
        )
        NavigationBarItem(
            selected = currentScreen == Screen.DELETE,
            onClick = { onNavigate(Screen.DELETE) },
            icon = { Text("U") },
            label = { Text("Usuń") }
        )
    }
}

@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { onNavigate(Screen.ADD) }, modifier = Modifier.fillMaxWidth()) {
            Text("Dodaj osobę")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onNavigate(Screen.LIST) }, modifier = Modifier.fillMaxWidth()) {
            Text("Wyświetl listę danych")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onNavigate(Screen.DELETE) }, modifier = Modifier.fillMaxWidth()) {
            Text("Usuń osobę")
        }
    }
}

@Composable
fun AddPersonScreen(onSave: (String, String, String, String, String, String) -> Unit) {
    var firstName by remember { mutableStateOf(TextFieldValue("")) }
    var lastName by remember { mutableStateOf(TextFieldValue("")) }
    var birthDate by remember { mutableStateOf(TextFieldValue("")) }
    var phone by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var address by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(firstName, { firstName = it }, label = { Text("Imię") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(lastName, { lastName = it }, label = { Text("Nazwisko") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(birthDate, { birthDate = it }, label = { Text("Data urodzenia") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(phone, { phone = it }, label = { Text("Telefon") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(address, { address = it }, label = { Text("Adres") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                onSave(
                    firstName.text,
                    lastName.text,
                    birthDate.text,
                    phone.text,
                    email.text,
                    address.text
                )
                firstName = TextFieldValue("")
                lastName = TextFieldValue("")
                birthDate = TextFieldValue("")
                phone = TextFieldValue("")
                email = TextFieldValue("")
                address = TextFieldValue("")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Zapisz")
        }
    }
}

@Composable
fun ListPersonsScreen(persons: List<Person>) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(persons) { person ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("${person.firstName} ${person.lastName}")
                    Text("Tel: ${person.phone}")
                    Text("Email: ${person.email}")
                    Text("Adres: ${person.address}")
                }
            }
        }
    }
}

@Composable
fun DeletePersonScreen(
    persons: List<Person>,
    onDelete: (Person) -> Unit
) {
    var search by remember { mutableStateOf(TextFieldValue("")) }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            label = { Text("Wyszukaj po nazwisku") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        val filtered = persons.filter {
            it.lastName.contains(search.text, ignoreCase = true)
        }

        LazyColumn {
            items(filtered) { person ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("${person.firstName} ${person.lastName}")
                            Text("Tel: ${person.phone}")
                        }
                        Button(onClick = { onDelete(person) }) {
                            Text("Usuń")
                        }
                    }
                }
            }
        }
    }
}
