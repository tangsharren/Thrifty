package my.edu.tarc.thrifty.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import my.edu.tarc.thrifty.R
import my.edu.tarc.thrifty.adapter.CartAdapter
import my.edu.tarc.thrifty.databinding.FragmentCartBinding
import my.edu.tarc.thrifty.roomdb.AppDatabase
import my.edu.tarc.thrifty.roomdb.ProductModel

class CartFragment : Fragment() {
    private lateinit var binding :FragmentCartBinding
    private lateinit var list : ArrayList<String>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(layoutInflater)

        val preference = requireContext().getSharedPreferences("info",AppCompatActivity.MODE_PRIVATE)
        val editor = preference.edit()
        editor.putBoolean("isCart",false)
        editor.apply()
        val dao = AppDatabase.getInstance(requireContext()).productDao()

        list = ArrayList()

        dao.getAllProducts().observe(requireActivity()){
            binding.cartRecycler.adapter = CartAdapter(requireContext(),it)
            list.clear()
            for(data in it){
                list.add(data.productId)
            }
            totalCost(it)
        }
        return binding.root

    }

    private fun totalCost(data: List<ProductModel>?) {
        var carbon = 0.0
        var total = 0.0
        for(item in data!!){
            total += item.productSp!!.toFloat()
            carbon += item.carbon!!.toFloat()
        }
        binding.tvCarbonSaved.text = String.format(getString(R.string.carbonTotal), carbon)
        binding.tvItemCount.text = getString(R.string.itemCart)+data.size
        binding.tvTotal.text = String.format(getString(R.string.costTotal), total)
        binding.btnCheckout.setOnClickListener {
            if(total == 0.0)
                Toast.makeText(requireContext(),getString(R.string.addProd),Toast.LENGTH_SHORT).show()
            else{
                Toast.makeText(requireContext(),getString(R.string.confirmAdd),Toast.LENGTH_SHORT).show()
                Log.d("MyApp",list.toTypedArray().toString())
                val action = CartFragmentDirections.actionCartFragmentToAddressFragment(list.toTypedArray(), total.toString())
                findNavController().navigate(action)
            }

        }
    }


}