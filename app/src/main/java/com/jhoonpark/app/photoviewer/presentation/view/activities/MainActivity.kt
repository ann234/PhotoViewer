package com.jhoonpark.app.photoviewer.presentation.view.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jhoonpark.app.photoviewer.R
import com.jhoonpark.app.photoviewer.databinding.ActivityMainBinding
import com.jhoonpark.app.photoviewer.presentation.view.adapters.PhotoAdapter
import com.jhoonpark.app.photoviewer.presentation.view.component.onThrottleClick
import com.jhoonpark.app.photoviewer.presentation.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val adapter = PhotoAdapter(this@MainActivity).apply {
            //  TODO
            //  현재 보고있던 item의 index를 사용해 같은 위치로 복원하는것이 아닌,
            //  item 자체를 따라가기 때문에 sorting을 수행할 경우 스크롤 position이 바뀌게됨.
            //  빗썸 앱은 정렬 시 스크롤 position을 그대로 두는 방식을 택하기 때문에
            //  빗썸 앱의 방식을 따라가는 것이 좋을듯 함.
            stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW
        }
        binding.rvPhotoView.layoutManager = LinearLayoutManager(this@MainActivity)
        binding.rvPhotoView.adapter = adapter

        viewModel.photoList.observe(this) { photos ->
            adapter.submitList(photos)
        }

        binding.buttonRead.onThrottleClick {
            lifecycleScope.launch {
                viewModel.updatePhotoList()
            }
        }

        binding.buttonClear.setOnClickListener {
            lifecycleScope.launch {
                viewModel.clearPhotoList()
            }
        }

        binding.buttonSort.setOnClickListener {
            lifecycleScope.launch {
                viewModel.sortPhotoList()
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}