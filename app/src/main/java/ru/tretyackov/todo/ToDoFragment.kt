package ru.tretyackov.todo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

private const val TODO_ID = "TODO_ID"

class ToDoFragment(private var toDoParam: ToDo? = null) : Fragment(), AdapterView.OnItemSelectedListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        toDoParam = initToDo(savedInstanceState)
        val toDo = toDoParam
        val view = inflater.inflate(R.layout.fragment_to_do, container, false)
        val editText = view.findViewById<EditText>(R.id.toDoDescriptionEditText)
        val closeImageButton = view.findViewById<ImageButton>(R.id.closeImageButton)
        val saveButton = view.findViewById<TextView>(R.id.saveButton)

        editText.setText(toDo?.name ?: "")

        val deleteTextView = view.findViewById<TextView>(R.id.deleteTextView)
        val deleteImageView = view.findViewById<ImageView>(R.id.deleteImageView)
        if(toDo != null)
        {
            val color = ContextCompat.getColor(requireContext(), R.color.deleteColor)
            deleteTextView.setTextColor(color)
            deleteImageView.setColorFilter(color)
        }
        val deleteClickListener = OnClickListener {
            if(toDo != null) {
                ToDoRepository.remove(toDo)
            }
            parentFragmentManager.popBackStack()
        }
        deleteTextView.setOnClickListener(deleteClickListener)
        deleteImageView.setOnClickListener(deleteClickListener)

        closeImageButton.setOnClickListener{
            parentFragmentManager.popBackStack()
        }

        val spinner = view.findViewById<Spinner>(R.id.spinner)
        spinner.onItemSelectedListener = this

        saveButton.setOnClickListener{
            if(toDo != null)
                ToDoRepository.update(toDo, ToDo(editText.text.toString().trim(),
                    toDo.completed, toDo.id, toDo.createdAt,
                    priority = ToDoPriority.values()[spinner.selectedItemPosition]))
            else
                ToDoRepository.add(ToDo(editText.text.toString().trim(), false,
                    priority = ToDoPriority.values()[spinner.selectedItemPosition]))
            parentFragmentManager.popBackStack()
        }

        return view
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {}

    override fun onNothingSelected(parent: AdapterView<*>) { }

    private fun initToDo(savedInstanceState: Bundle?) : ToDo?
    {
        if(toDoParam!=null)
            return toDoParam
        val id = savedInstanceState?.getString(TODO_ID, null)
        if(id != null)
            return ToDoRepository.find(id)
        return null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(toDoParam!=null)
        {
            outState.putString(TODO_ID, toDoParam!!.id)
        }
    }
}