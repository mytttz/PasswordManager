import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanager.CreateOrEditPasswordActivity
import com.example.passwordmanager.MyEncryptedSharedPreferences
import com.example.passwordmanager.R
import com.example.passwordmanager.database.Password
import com.squareup.picasso.Picasso

class PasswordAdapter(
    private val context: Context,
    private var passwordList: List<Password>,
    private val masterKey: String,
    private val parent: ViewGroup
) :
    RecyclerView.Adapter<PasswordAdapter.PasswordViewHolder>() {

    inner class PasswordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val siteTextView: TextView = itemView.findViewById(R.id.siteNameText)
        val loginTextView: TextView = itemView.findViewById(R.id.loginText)
        val passwordTextView: TextView = itemView.findViewById(R.id.passwordText)
        private val editButton: ImageView = itemView.findViewById(R.id.editButton)
        private val visibilityOfPass: ImageView = itemView.findViewById(R.id.visibilityOfPass)
        val siteImg: ImageView = itemView.findViewById(R.id.siteImg)

        init {
            editButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val intent = Intent(context, CreateOrEditPasswordActivity::class.java)
                    intent.putExtra("EXTRA_ID", passwordList[position].id)
                    context.startActivity(intent)
                }
            }

            passwordTextView.setOnClickListener {
                MyEncryptedSharedPreferences.initialize(context, masterKey)
                val encryptedSharedPreferences =
                    MyEncryptedSharedPreferences.getEncryptedSharedPreferences()
                val key =
                    "${passwordList[position].site} ${passwordList[position].login}".hashCode()
                        .toString()
                val passwordToCopy = encryptedSharedPreferences.getString(key, "").toString()
                val clipboardManager =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("text", passwordToCopy)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()

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
                    val dots = "•".repeat(passwordTextView.text.length)
                    passwordTextView.text = dots
                }
            }
            visibilityOfPass.setOnClickListener {
                val dots = "•".repeat(passwordTextView.text.length)
                if (passwordTextView.text == dots) {
                    visibilityOfPass.setImageResource(R.drawable.visibility_off_icon)
                    val encryptedSharedPreferences =
                        MyEncryptedSharedPreferences.getEncryptedSharedPreferences()
                    val key =
                        "${passwordList[position].site} ${passwordList[position].login}".hashCode()
                            .toString()
                    val password = encryptedSharedPreferences.getString(key, "").toString()
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
        val currentItem = passwordList[position]
        holder.siteTextView.text = currentItem.site
        Picasso.get()
            .load("https://logo.clearbit.com/${holder.siteTextView.text}")
            .transform(CircleTransformation())
            .placeholder(R.drawable.download_icon) // Заглушка, отображаемая во время загрузки
            .error(R.drawable.key_icon) // Заглушка, отображаемая при ошибке загрузки
            .into(holder.siteImg)
        holder.loginTextView.text = currentItem.login
        val encryptedSharedPreferences =
            MyEncryptedSharedPreferences.getEncryptedSharedPreferences()
        val key =
            "${passwordList[position].site} ${passwordList[position].login}".hashCode().toString()
        val password = encryptedSharedPreferences.getString(key, "").toString()
        val dots = "•".repeat(password.length)
        holder.passwordTextView.text = dots
        holder.loginTextView.visibility = View.GONE
        holder.passwordTextView.visibility = View.GONE
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<Password>) {
        passwordList = items
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return passwordList.size
    }
}