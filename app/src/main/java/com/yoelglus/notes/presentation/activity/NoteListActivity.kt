package com.yoelglus.notes.presentation.activity


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.yoelglus.notes.R
import com.yoelglus.notes.domain.Note
import com.yoelglus.notes.presentation.fragment.NoteDetailFragment
import com.yoelglus.notes.presentation.presenter.NotesListPresenter
import com.yoelglus.notes.presentation.presenter.PresenterFactory
import kotlinx.android.synthetic.main.activity_note_list.*

class NoteListActivity : AppCompatActivity(), NotesListPresenter.View {

    private val ADD_NOTE_REQUEST = 123

    private var twoPane: Boolean = false

    private val presenter: NotesListPresenter by lazy {
        PresenterFactory.createNotesListPresenter(this)
    }

    private val adapter = SimpleItemRecyclerViewAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter.takeView(this)

        setContentView(R.layout.activity_note_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        addNoteButton.setOnClickListener { view ->
            startActivityForResult(Intent(view.context, AddNoteActivity::class.java), ADD_NOTE_REQUEST)
        }

        findViewById<RecyclerView>(R.id.note_list).adapter = adapter

        if (findViewById<FrameLayout>(R.id.note_detail_container) != null) {
            twoPane = true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_NOTE_REQUEST && resultCode == Activity.RESULT_OK) {
            presenter.refreshData()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.dropView()
    }

    override fun showNotes(notes: List<Note>) {
        adapter.values.clear()
        adapter.values.addAll(notes)
        adapter.notifyDataSetChanged()
    }

    override fun showError(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    inner class SimpleItemRecyclerViewAdapter : RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        val values: MutableList<Note> = mutableListOf()

        override fun getItemCount(): Int {
            return values.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.note_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.item = values[position]
            holder.idView.text = values[position].title
            holder.contentView.text = values[position].text

            holder.mView.setOnClickListener { v ->
                if (twoPane) {
                    val arguments = Bundle()
                    arguments.putInt(NoteDetailFragment.ARG_ITEM_ID, holder.item.id)
                    val fragment = NoteDetailFragment()
                    fragment.arguments = arguments
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.note_detail_container, fragment)
                            .commit()
                } else {
                    val context = v.context
                    val intent = Intent(context, NoteDetailActivity::class.java)
                    intent.putExtra(NoteDetailFragment.ARG_ITEM_ID, holder.item.id)

                    context.startActivity(intent)
                }
            }
        }

        inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
            val idView: TextView = mView.findViewById(R.id.id)
            val contentView: TextView = mView.findViewById(R.id.content)
            lateinit var item: Note

            override fun toString(): String {
                return super.toString() + " '" + contentView.text + "'"
            }
        }
    }
}
