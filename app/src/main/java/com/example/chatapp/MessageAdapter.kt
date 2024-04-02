package com.example.chatapp

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.ReceiveBinding
import com.example.chatapp.databinding.SentBinding
import com.example.chatapp.databinding.UserLayoutBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.persistentCacheSettings
import java.util.regex.Pattern

class MessageAdapter(val context: Context, val messageList: ArrayList<Message>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val itemReceive = 1
    val itemSent = 2
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == 1) {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ReceiveBinding.inflate(inflater, parent, false)
            ReceiveViewHolder(binding)
        } else {
            val inflater = LayoutInflater.from(parent.context)
            val binding = SentBinding.inflate(inflater, parent, false)
            SentViewHolder(binding)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        return if(FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderId)) {
            itemSent
        } else {
            itemReceive
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]

        val listOfUrls = getHyperLinks(currentMessage.message.toString())
        val listOfEmails = getEmailLists(currentMessage.message.toString())
        val spanPoint = SpannableString(currentMessage.message.toString())
        var point: SpannableString? = null
        for (url in listOfUrls) {
            point = customiseText(spanPoint, url.first, url.second)
        }
        for (email in listOfEmails) {
            point = customiseText(spanPoint, email.first, email.second)
        }
//If our string doesnot contain any hyperlink we will set out textview with original string ortherwise with our new customised string.
        if (point != null) {
            currentMessage.message = point.toString()
        }

        if(holder.javaClass == SentViewHolder::class.java) {
            val viewHolder = holder as SentViewHolder
            viewHolder.sentMessage.text = currentMessage.message
        } else {
            val viewHolder = holder as ReceiveViewHolder
            viewHolder.receiveMessage.text = currentMessage.message
        }

    }

    class SentViewHolder(binding: SentBinding) : RecyclerView.ViewHolder(binding.root) {
        val sentMessage = binding.idSent
    }
    class ReceiveViewHolder(binding: ReceiveBinding) : RecyclerView.ViewHolder(binding.root) {
        val receiveMessage = binding.idReceive
    }

    private val urlPattern: Pattern = Pattern.compile(
        "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
        Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL
    )
    private val emailPattern: Pattern = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )
    //Function to extract hyperlinks from a given string
    private fun getHyperLinks(s: String): List<Pair<Int, Int>> {
        val urlList = mutableListOf<Pair<Int, Int>>()
        val urlMatcher = urlPattern.matcher(s)
        var matchStart: Int
        var matchEnd: Int
        while (urlMatcher.find()) {
            matchStart = urlMatcher.start(1)
            matchEnd = urlMatcher.end()
            urlList.add(Pair(matchStart, matchEnd))
            val url = s.substring(matchStart, matchEnd)
        }
        return urlList
    }

    //Function to extract emails from a given string
    private fun getEmailLists(s: String): List<Pair<Int, Int>> {
        val emailList = mutableListOf<Pair<Int, Int>>()

        val emailMatcher = emailPattern.matcher(s)
        while (emailMatcher.find()) {
            val email = emailMatcher.group()
            emailList.add(Pair(emailMatcher.start(), emailMatcher.start() + email.length))
        }
        return emailList
    }

    //Function to customise texts which are identified as a hyperlink or an email
    private fun customiseText(
        spanStr: SpannableString,
        start: Int,
        end: Int
    ): SpannableString {
        val clickSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Write the actions you want to be performed on click of the particular hyperlink or email
            }
        }
        spanStr.setSpan(clickSpan, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
//Change the colour of the hyperlink or the email
        spanStr.setSpan(
            ForegroundColorSpan(Color.BLACK),
            start, end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spanStr
    }

}