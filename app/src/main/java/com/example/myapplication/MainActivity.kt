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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

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

    fun updatePerson(person: Person): Int {
        val values = ContentValues().apply {
            put("first_name", person.firstName)
            put("last_name", person.lastName)
            put("birth_date", person.birthDate)
            put("phone", person.phone)
            put("email", person.email)
            put("address", person.address)
        }
        return writableDatabase.update(
            "persons",
            values,
            "id = ?",
            arrayOf(person.id.toString())
        )
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
        setContent { PersonApp() }
    }
}

enum class AppLang { PL, EN }

data class Strings(
    val homeTitle: String,
    val addTitle: String,
    val listTitle: String,
    val deleteTitle: String,
    val privacyTitle: String,
    val editTitle: String,

    val tabAdd: String,
    val tabList: String,
    val tabDelete: String,

    val btnAddPerson: String,
    val btnShowList: String,
    val btnDeletePerson: String,
    val btnPrivacy: String,

    val firstName: String,
    val lastName: String,
    val birthDate: String,
    val phone: String,
    val email: String,
    val address: String,

    val save: String,
    val update: String,
    val searchByLastName: String,

    val requiredFieldsMsg: String,

    val delete: String,
    val deleteConfirmTitle: String,
    val deleteConfirmText: String,
    val cancel: String,
    val confirm: String,

    val privacyBody: String
)

private fun stringsFor(lang: AppLang): Strings = when (lang) {
    AppLang.PL -> Strings(
        homeTitle = "Ekran główny",
        addTitle = "Dodaj osobę",
        listTitle = "Lista osób",
        deleteTitle = "Usuń osobę",
        privacyTitle = "Polityka prywatności",
        editTitle = "Edytuj dane",

        tabAdd = "Dodaj",
        tabList = "Lista",
        tabDelete = "Usuń",

        btnAddPerson = "Dodaj osobę",
        btnShowList = "Wyświetl listę danych",
        btnDeletePerson = "Usuń osobę",
        btnPrivacy = "Polityka prywatności",

        firstName = "Imię",
        lastName = "Nazwisko",
        birthDate = "Data urodzenia",
        phone = "Telefon",
        email = "Email",
        address = "Adres",

        save = "Zapisz",
        update = "Zapisz zmiany",
        searchByLastName = "Wyszukaj po nazwisku",

        requiredFieldsMsg = "Wszystkie pola są wymagane — uzupełnij je.",

        delete = "Usuń",
        deleteConfirmTitle = "Potwierdź usunięcie",
        deleteConfirmText = "Na pewno chcesz usunąć tę osobę?",
        cancel = "Anuluj",
        confirm = "Tak, usuń",

        privacyBody =
        "Twoje dane są przechowywane lokalnie w bazie SQLite na urządzeniu.\n" +
                "Aplikacja nie wysyła danych do internetu.\n" +
                "Możesz usunąć lub edytować wpisy w dowolnym momencie."
    )

    AppLang.EN -> Strings(
        homeTitle = "Home",
        addTitle = "Add person",
        listTitle = "People list",
        deleteTitle = "Delete person",
        privacyTitle = "Privacy policy",
        editTitle = "Edit details",

        tabAdd = "Add",
        tabList = "List",
        tabDelete = "Delete",

        btnAddPerson = "Add person",
        btnShowList = "Show data list",
        btnDeletePerson = "Delete person",
        btnPrivacy = "Privacy policy",

        firstName = "First name",
        lastName = "Last name",
        birthDate = "Birth date",
        phone = "Phone",
        email = "Email",
        address = "Address",

        save = "Save",
        update = "Save changes",
        searchByLastName = "Search by last name",

        requiredFieldsMsg = "All fields are required — please fill them in.",

        delete = "Delete",
        deleteConfirmTitle = "Confirm deletion",
        deleteConfirmText = "Are you sure you want to delete this person?",
        cancel = "Cancel",
        confirm = "Yes, delete",

        privacyBody =
        "Your data is stored locally in an SQLite database on this device.\n" +
                "The app does not send data to the internet.\n" +
                "You can edit or delete entries at any time."
    )
}

enum class Screen {
    HOME,
    ADD,
    LIST,
    DELETE,
    PRIVACY,
    EDIT
}

@Composable
fun PersonApp() {
    val context = LocalContext.current
    val db = remember { PersonDatabaseHelper(context) }

    val persons = remember {
        mutableStateListOf<Person>().apply { addAll(db.getAllPersons()) }
    }

    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var lang by remember { mutableStateOf(AppLang.PL) }
    val s = remember(lang) { stringsFor(lang) }
    var selectedPerson by remember { mutableStateOf<Person?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(enabled = currentScreen != Screen.HOME) {
        currentScreen = Screen.HOME
        selectedPerson = null
    }

    val onSavePerson: (String, String, String, String, String, String) -> Unit =
        { f, l, b, p, e, a ->
            val tempPerson = Person(0, f, l, b, p, e, a)
            val newId = db.insertPerson(tempPerson)
            if (newId != -1L) persons.add(tempPerson.copy(id = newId))
        }

    val onUpdatePerson: (Person) -> Unit = { updated ->
        val rows = db.updatePerson(updated)
        if (rows > 0) {
            val idx = persons.indexOfFirst { it.id == updated.id }
            if (idx != -1) persons[idx] = updated
        }
    }

    val onDeletePerson: (Person) -> Unit = { person ->
        db.deletePerson(person.id)
        persons.remove(person)
    }

    MaterialTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            when (currentScreen) {
                                Screen.HOME -> s.homeTitle
                                Screen.ADD -> s.addTitle
                                Screen.LIST -> s.listTitle
                                Screen.DELETE -> s.deleteTitle
                                Screen.PRIVACY -> s.privacyTitle
                                Screen.EDIT -> s.editTitle
                            }
                        )
                    },
                    navigationIcon = {
                        if (currentScreen != Screen.HOME) {
                            IconButton(onClick = {
                                currentScreen = Screen.HOME
                                selectedPerson = null
                            }) { Text("<") }
                        }
                    },
                    actions = {
                        TextButton(onClick = { lang = if (lang == AppLang.PL) AppLang.EN else AppLang.PL }) {
                            Text(if (lang == AppLang.PL) "EN" else "PL", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            },
            bottomBar = {
                BottomTabBar(
                    currentScreen = currentScreen,
                    onNavigate = { currentScreen = it },
                    s = s
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (currentScreen) {
                    Screen.HOME -> HomeScreen(
                        s = s,
                        onNavigate = { currentScreen = it }
                    )

                    Screen.ADD -> AddPersonScreen(
                        s = s,
                        snackbarHostState = snackbarHostState,
                        onSave = onSavePerson
                    )

                    Screen.LIST -> ListPersonsScreen(
                        s = s,
                        persons = persons,
                        onEditClick = { p ->
                            selectedPerson = p
                            currentScreen = Screen.EDIT
                        }
                    )

                    Screen.EDIT -> {
                        val p = selectedPerson
                        if (p != null) {
                            EditPersonScreen(
                                s = s,
                                person = p,
                                snackbarHostState = snackbarHostState,
                                onSaveChanges = { updated ->
                                    onUpdatePerson(updated)
                                    currentScreen = Screen.LIST
                                    selectedPerson = null
                                }
                            )
                        } else {
                            currentScreen = Screen.LIST
                        }
                    }

                    Screen.DELETE -> DeletePersonScreen(
                        s = s,
                        persons = persons,
                        onDelete = onDeletePerson
                    )

                    Screen.PRIVACY -> PrivacyScreen(s = s)
                }
            }
        }
    }
}

@Composable
fun BottomTabBar(currentScreen: Screen, onNavigate: (Screen) -> Unit, s: Strings) {
    NavigationBar {
        NavigationBarItem(
            selected = currentScreen == Screen.ADD,
            onClick = { onNavigate(Screen.ADD) },
            icon = { Text("A") },
            label = { Text(s.tabAdd) }
        )
        NavigationBarItem(
            selected = currentScreen == Screen.LIST || currentScreen == Screen.EDIT,
            onClick = { onNavigate(Screen.LIST) },
            icon = { Text("L") },
            label = { Text(s.tabList) }
        )
        NavigationBarItem(
            selected = currentScreen == Screen.DELETE,
            onClick = { onNavigate(Screen.DELETE) },
            icon = { Text("U") },
            label = { Text(s.tabDelete) }
        )
    }
}

@Composable
fun HomeScreen(s: Strings, onNavigate: (Screen) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { onNavigate(Screen.ADD) }, modifier = Modifier.fillMaxWidth()) {
            Text(s.btnAddPerson)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onNavigate(Screen.LIST) }, modifier = Modifier.fillMaxWidth()) {
            Text(s.btnShowList)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onNavigate(Screen.DELETE) }, modifier = Modifier.fillMaxWidth()) {
            Text(s.btnDeletePerson)
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = { onNavigate(Screen.PRIVACY) }, modifier = Modifier.fillMaxWidth()) {
            Text(s.btnPrivacy)
        }
    }
}

@Composable
fun AddPersonScreen(
    s: Strings,
    snackbarHostState: SnackbarHostState,
    onSave: (String, String, String, String, String, String) -> Unit
) {
    val scope = rememberCoroutineScope()

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
        OutlinedTextField(firstName, { firstName = it }, label = { Text(s.firstName) }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(lastName, { lastName = it }, label = { Text(s.lastName) }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(birthDate, { birthDate = it }, label = { Text(s.birthDate) }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(phone, { phone = it }, label = { Text(s.phone) }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(email, { email = it }, label = { Text(s.email) }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(address, { address = it }, label = { Text(s.address) }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val values = listOf(
                    firstName.text, lastName.text, birthDate.text,
                    phone.text, email.text, address.text
                )

                if (values.any { it.isBlank() }) {
                    scope.launch { snackbarHostState.showSnackbar(s.requiredFieldsMsg) }
                    return@Button
                }

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
            Text(s.save)
        }
    }
}

@Composable
fun ListPersonsScreen(
    s: Strings,
    persons: List<Person>,
    onEditClick: (Person) -> Unit
) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(persons) { person ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onEditClick(person) }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("${person.firstName} ${person.lastName}", fontWeight = FontWeight.Bold)
                    Text("${s.phone}: ${person.phone}")
                    Text("${s.email}: ${person.email}")
                    Text("${s.address}: ${person.address}")
                }
            }
        }
    }
}

@Composable
fun EditPersonScreen(
    s: Strings,
    person: Person,
    snackbarHostState: SnackbarHostState,
    onSaveChanges: (Person) -> Unit
) {
    val scope = rememberCoroutineScope()

    var firstName by remember { mutableStateOf(TextFieldValue(person.firstName)) }
    var lastName by remember { mutableStateOf(TextFieldValue(person.lastName)) }
    var birthDate by remember { mutableStateOf(TextFieldValue(person.birthDate)) }
    var phone by remember { mutableStateOf(TextFieldValue(person.phone)) }
    var email by remember { mutableStateOf(TextFieldValue(person.email)) }
    var address by remember { mutableStateOf(TextFieldValue(person.address)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(firstName, { firstName = it }, label = { Text(s.firstName) }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(lastName, { lastName = it }, label = { Text(s.lastName) }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(birthDate, { birthDate = it }, label = { Text(s.birthDate) }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(phone, { phone = it }, label = { Text(s.phone) }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(email, { email = it }, label = { Text(s.email) }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(address, { address = it }, label = { Text(s.address) }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val values = listOf(
                    firstName.text, lastName.text, birthDate.text,
                    phone.text, email.text, address.text
                )
                if (values.any { it.isBlank() }) {
                    scope.launch { snackbarHostState.showSnackbar(s.requiredFieldsMsg) }
                    return@Button
                }

                onSaveChanges(
                    person.copy(
                        firstName = firstName.text,
                        lastName = lastName.text,
                        birthDate = birthDate.text,
                        phone = phone.text,
                        email = email.text,
                        address = address.text
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(s.update)
        }
    }
}

@Composable
fun DeletePersonScreen(
    s: Strings,
    persons: List<Person>,
    onDelete: (Person) -> Unit
) {
    var search by remember { mutableStateOf(TextFieldValue("")) }
    var confirmDialogOpen by remember { mutableStateOf(false) }
    var personToDelete by remember { mutableStateOf<Person?>(null) }

    if (confirmDialogOpen && personToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                confirmDialogOpen = false
                personToDelete = null
            },
            title = { Text(s.deleteConfirmTitle) },
            text = { Text(s.deleteConfirmText) },
            confirmButton = {
                TextButton(onClick = {
                    personToDelete?.let(onDelete)
                    confirmDialogOpen = false
                    personToDelete = null
                }) { Text(s.confirm) }
            },
            dismissButton = {
                TextButton(onClick = {
                    confirmDialogOpen = false
                    personToDelete = null
                }) { Text(s.cancel) }
            }
        )
    }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            label = { Text(s.searchByLastName) },
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
                            Text("${person.firstName} ${person.lastName}", fontWeight = FontWeight.Bold)
                            Text("${s.phone}: ${person.phone}")
                        }
                        Button(onClick = {
                            personToDelete = person
                            confirmDialogOpen = true
                        }) {
                            Text(s.delete)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacyScreen(s: Strings) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(s.privacyTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(s.privacyBody, style = MaterialTheme.typography.bodyLarge)
    }
}
