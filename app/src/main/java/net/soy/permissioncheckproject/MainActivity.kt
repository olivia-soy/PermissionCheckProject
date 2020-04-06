package net.soy.permissioncheckproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {
    private var mFragment: Fragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(savedInstanceState == null) {
            mFragment = GalleryOrCameraFragment()
            mFragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout_container, it)
                    .commit()
            }
        }
    }
}
