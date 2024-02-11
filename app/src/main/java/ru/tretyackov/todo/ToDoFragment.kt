package ru.tretyackov.todo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat

class ToDoFragment(private val toDoList: ToDoList, private val toDo: ToDo?) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_to_do, container, false)
        val editText = view.findViewById<EditText>(R.id.toDoDescriptionEditText)
        val closeImageButton = view.findViewById<ImageButton>(R.id.closeImageButton)
        val saveButton = view.findViewById<TextView>(R.id.saveButton)

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
                toDoList.remove(toDo)
            }
            parentFragmentManager.popBackStack()
        }
        deleteTextView.setOnClickListener(deleteClickListener)
        deleteImageView.setOnClickListener(deleteClickListener)

        editText.setText(toDo?.name ?: "")
        closeImageButton.setOnClickListener{
            parentFragmentManager.popBackStack()
        }
        saveButton.setOnClickListener{
            if(toDo != null)
                toDoList.update(toDo, ToDo(toDo.id, editText.text.toString(), toDo.completed, toDo.createdAt))
            else
                toDoList.add(ToDo("123", editText.text.toString(), false))
            parentFragmentManager.popBackStack()
        }

        return view
    }
}