package ru.tretyackov.todo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import kotlinx.coroutines.runBlocking
import ru.tretyackov.todo.data.getYandexAuthToken
import ru.tretyackov.todo.ui.LoginFragment
import ru.tretyackov.todo.ui.ToDoListFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val token = runBlocking {
            return@runBlocking this@MainActivity.getYandexAuthToken()
        }
        val nowAtUtc = System.currentTimeMillis()
        val fragmentClass =
            if (token == null || token.expiresAtUtc <= nowAtUtc) LoginFragment::class.java else ToDoListFragment::class.java
        supportFragmentManager.commit {
            replace(R.id.fragment_container_view, fragmentClass, null)
        }
    }
}