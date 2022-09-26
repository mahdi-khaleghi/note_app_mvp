package com.example.noteappmvp.ui.main

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.noteappmvp.R
import com.example.noteappmvp.data.model.NoteEntity
import com.example.noteappmvp.data.repository.main.MainRepository
import com.example.noteappmvp.databinding.ActivityMainBinding
import com.example.noteappmvp.ui.note.NoteFragment
import com.example.noteappmvp.utils.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), MainContract.View {

    //Binding
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var repository: MainRepository

    @Inject
    lateinit var noteAdapter: NoteAdapter

    @Inject
    lateinit var presenter: MainPresenter

    //Other
    //private val presenter by lazy { MainPresenter(repository, this) }
    private var selectedItem = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //InitViews
        binding.apply {
            //Set action view
            setSupportActionBar(notesToolbar    )
            //Note detail
            addNoteBtn.setOnClickListener {
                NoteFragment().show(
                    supportFragmentManager,
                    NoteFragment().tag
                )
            }
            //Load all notes
            presenter.loadAllNotes()
            //Clicks
            noteAdapter.setOnItemClickListener { entity, state ->
                when (state) {
                    EDIT -> {
                        val noteFragment = NoteFragment()
                        val bundle = Bundle()
                        bundle.putInt(BUNDLE_ID, entity.id)
                        noteFragment.arguments = bundle
                        noteFragment.show(supportFragmentManager, NoteFragment().tag)
                    }
                    DELETE -> {
                        val noteEntity = NoteEntity(
                            entity.id,
                            entity.title,
                            entity.desc,
                            entity.category,
                            entity.priority
                        )
                        presenter.deleteNote(noteEntity)
                    }
                }
            }
            //Filter
            notesToolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.actionFilter -> {
                        filterByPriority()
                        return@setOnMenuItemClickListener true
                    }
                    else -> {
                        return@setOnMenuItemClickListener false
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        val search = menu.findItem(R.id.actionSearch)
        val searchView = search.actionView as SearchView
        searchView.queryHint = "Search..."
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                presenter.searchNote(newText)
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun showAllNotes(notes: List<NoteEntity>) {
        binding.emptyLay.visibility = View.GONE
        binding.noteList.visibility = View.VISIBLE

        noteAdapter.setData(notes)

        binding.noteList.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = noteAdapter
        }
    }

    override fun showEmpty() {
        binding.emptyLay.visibility = View.VISIBLE
        binding.noteList.visibility = View.GONE
    }

    override fun deleteMessage() {
        Snackbar.make(binding.root, "Note deleted!", Snackbar.LENGTH_SHORT).show()
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }

    private fun filterByPriority() {
        val builder = AlertDialog.Builder(this)

        val priorities = arrayOf(ALL, HIGH, NORMAL, LOW)

        builder.setSingleChoiceItems(priorities, selectedItem) { dialog, item ->
            when (item) {
                0 -> {
                    presenter.loadAllNotes()
                }
                in 1..3 -> {
                    presenter.filterNote(priorities[item])
                }
            }
            selectedItem = item
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}