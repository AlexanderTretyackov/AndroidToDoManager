package ru.tretyackov.todo.ui

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.yandex.div.DivDataTag
import com.yandex.div.core.Div2Context
import com.yandex.div.core.DivConfiguration
import com.yandex.div.core.view2.Div2View
import com.yandex.div.picasso.PicassoDivImageLoader
import kotlinx.coroutines.launch
import ru.tretyackov.todo.R
import ru.tretyackov.todo.utilities.AssetReader
import ru.tretyackov.todo.utilities.asDiv2DataWithTemplates

class AboutAppFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        val navigationDivActionHandler = NavigationDivActionHandler()
        lifecycleScope.launch {
            navigationDivActionHandler.openScreenState.collect { screen ->
                if (screen == Screen.TodoList) {
                    parentFragmentManager.popBackStack()
                }
            }
        }
        val configuration =
            DivConfiguration.Builder(PicassoDivImageLoader(requireContext()))
                .actionHandler(navigationDivActionHandler).build()
        val cardJsonObject = AssetReader(requireContext()).read("about_app.json")
        val divData = cardJsonObject.asDiv2DataWithTemplates()
        val div2View = Div2View(
            Div2Context(
                this.requireActivity(),
                configuration,
                R.style.Theme_ToDo,
                viewLifecycleOwner
            )
        )
        div2View.setData(divData, DivDataTag("tag"))
        div2View.setVariable("dark_mode", checkNightMode().toString())
        val rootView =
            inflater.inflate(R.layout.fragment_about_app, container, false) as LinearLayout
        rootView.addView(div2View)
        return rootView
    }

    private fun checkNightMode(): Boolean {
        val nightModeFlags: Int = requireContext().resources
            .configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }
}
