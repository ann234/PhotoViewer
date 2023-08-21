package com.jhoonpark.app.photoviewer.presentation.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jhoonpark.app.photoviewer.databinding.ItemPhotoLayoutBinding
import com.jhoonpark.app.photoviewer.domain.model.PhotoDataModel
import com.bumptech.glide.Glide

class PhotoAdapter(private val context: Context) :
    ListAdapter<PhotoDataModel, PhotoAdapter.ViewHolder>(DiffUtilCallback<PhotoDataModel>(
        compareItems = { item1, item2 -> item1.uuid == item2.uuid },
        compareContents = { item1, item2 -> item1 == item2 }
    )) {

    inner class ViewHolder(view: ItemPhotoLayoutBinding) : RecyclerView.ViewHolder(view.root) {
        // Holds the TextView that will add each item to
        private val ivPhotoImage = view.ivPhoto
        private val tvPhotoTitle = view.tvPhotoTitle
        private val tvPhotoId = view.tvPhotoId

        fun bind(photo: PhotoDataModel) {
            Glide.with(context)
                .load(photo.thumbnailUrl)
                .into(ivPhotoImage)
            //  Use codes below if use original image
//            Glide.with(context)
//                .load(photo.url)
//                .thumbnail(
//                    Glide.with(context)
//                        .load(photo.thumbnailUrl)
//                )
//                .into(holder.ivPhotoImage)

            tvPhotoTitle.text = photo.title
            tvPhotoId.text = photo.id.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemPhotoLayoutBinding =
            ItemPhotoLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val photo = getItem(position)
        holder.bind(photo)
    }
}

fun interface CompareItem<T> {
    fun compare(item1: T, item2: T): Boolean
}

fun interface CompareContent<T> {
    fun compare(item1: T, item2: T): Boolean
}

class DiffUtilCallback<T : Any> (
    private val compareItems: CompareItem<T>,
    private val compareContents: CompareContent<T>,
): DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = compareItems.compare(oldItem, newItem)

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = compareContents.compare(oldItem, newItem)
}