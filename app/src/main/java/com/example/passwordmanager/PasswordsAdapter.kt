import android.content.Context
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanager.R
import com.example.passwordmanager.passwordlist.PasswordItemState
import com.example.passwordmanager.passwordlist.PasswordListViewModel
import com.squareup.picasso.Picasso

class PasswordAdapter(
    private val context: Context,
    private val parent: ViewGroup,
    private val viewModel: PasswordListViewModel
) :
    ListAdapter<PasswordItemState, PasswordAdapter.PasswordViewHolder>(PasswordDiffCallback()) {

    inner class PasswordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val siteTextView: TextView = itemView.findViewById(R.id.siteNameText)
        val loginTextView: TextView = itemView.findViewById(R.id.loginText)
        val passwordTextView: TextView = itemView.findViewById(R.id.passwordText)
        private val editButton: ImageView = itemView.findViewById(R.id.editButton)
        private val visibilityOfPass: ImageView = itemView.findViewById(R.id.visibilityOfPass)
        val siteImg: ImageView = itemView.findViewById(R.id.siteImg)

        init {
            editButton.setOnClickListener {
                viewModel.editPassword(context, getItem(adapterPosition).id)
            }

            passwordTextView.setOnClickListener {
                viewModel.copyPassword(context, getItem(adapterPosition).itemPass)
            }

            itemView.setOnClickListener {
                val transition = AutoTransition()
                transition.duration = 150
                TransitionManager.endTransitions(parent)
                TransitionManager.beginDelayedTransition(parent, transition)

                if (loginTextView.visibility == View.GONE && passwordTextView.visibility == View.GONE) {
                    loginTextView.visibility = View.VISIBLE
                    passwordTextView.visibility = View.VISIBLE
                    visibilityOfPass.visibility = View.VISIBLE
                } else {
                    loginTextView.visibility = View.GONE
                    passwordTextView.visibility = View.GONE
                    visibilityOfPass.visibility = View.GONE
                    visibilityOfPass.setImageResource(R.drawable.visibility_icon)
                    val dots = "•".repeat(passwordTextView.text.length)
                    passwordTextView.text = dots
                }

            }

            visibilityOfPass.setOnClickListener {
                val password = getItem(adapterPosition).itemPass
                val dots = "•".repeat(password.length)
                if (passwordTextView.text == dots) {
                    visibilityOfPass.setImageResource(R.drawable.visibility_off_icon)
                    passwordTextView.text = password
                } else {
                    visibilityOfPass.setImageResource(R.drawable.visibility_icon)
                    passwordTextView.text = dots
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasswordViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.password_list_item, parent, false)
        return PasswordViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PasswordViewHolder, position: Int) {
        val password = getItem(position).itemPass
        holder.loginTextView.visibility = View.GONE
        holder.passwordTextView.visibility = View.GONE
        holder.passwordTextView.text = "•".repeat(password.length)
        val currentItem = getItem(position)
        holder.siteTextView.text = currentItem.itemSite
        holder.loginTextView.text = currentItem.itemLogin
        Picasso.get()
            .load("https://logo.clearbit.com/${holder.siteTextView.text}")
            .transform(CircleTransformation())
            .placeholder(R.drawable.download_icon) // Заглушка, отображаемая во время загрузки
            .error(R.drawable.key_icon) // Заглушка, отображаемая при ошибке загрузки
            .into(holder.siteImg)
    }

    override fun submitList(list: List<PasswordItemState>?) {
        val transition = AutoTransition()
        transition.duration = 150
        TransitionManager.endTransitions(parent)
        TransitionManager.beginDelayedTransition(parent, transition)
        super.submitList(list)
    }

    class PasswordDiffCallback : DiffUtil.ItemCallback<PasswordItemState>() {
        override fun areItemsTheSame(
            oldItem: PasswordItemState,
            newItem: PasswordItemState
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: PasswordItemState,
            newItem: PasswordItemState
        ): Boolean {
            return oldItem == newItem
        }
    }
}