package mutnemom.android.kotlindemo.recyclerview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import mutnemom.android.kotlindemo.databinding.ActivityListAdapterDemoBinding
import kotlin.random.Random

class ListAdapterDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListAdapterDemoBinding
    private var personAdapter: PersonListAdapter? = null

    private val initialData: List<Person> = listOf(
        Person(
            name = "Billy",
            email = "billy@hytexts.com",
            avatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQu_fpPmbK-bebEeX036y7frmW06amtCkG1ew&usqp=CAU"
        ),
        Person(
            name = "Jones",
            email = "jones@hytexts.com",
            avatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTSIsmpJQm0OTBcGyY-Y3ECq4UMpN2lAcagoQ&usqp=CAU"
        ),
        Person(
            name = "Chet Haase",
            email = "chethaase@hytexts.com",
            avatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQHwH1mT6aap9Ad21ZO4Qi931A1fXXo47zirJwsr5EbgabWAS9mk_MMPsWVATJdrgYMZC0&usqp=CAU"
        ),
        Person(
            name = "Ake",
            email = "ake@hytexts.com",
            avatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRxf9IiK7J06r9bR8u7dNRyRFi-NAwKx6noOAMCMC_cByU2UdPXE59AB4yZHJdMELXI4l4&usqp=CAU"
        ),
        Person(
            name = "Blue",
            email = "blue@hytexts.com"
        ),
        Person(
            name = "Bent",
            email = "bent@hytexts.com",
            avatar = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQgm6d98mViWYkm1iKeuy5SoiXa63tejxkQSg&usqp=CAU"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListAdapterDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setEvent()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            personAdapter = PersonListAdapter(initialData)
            adapter = personAdapter
            layoutManager = LinearLayoutManager(this@ListAdapterDemoActivity)
        }
    }

    private fun setEvent() {
        with(binding) {
            btnRandom.setOnClickListener { shuffleData() }
            btnReset.setOnClickListener { resetData() }
        }
    }

    private fun shuffleData() {
        val num1 = Random.nextInt(0, initialData.size)
        val num2 = Random.nextInt(0, initialData.size)

        personAdapter?.list = initialData.asReversed()
            .subList(minOf(num1, num2), maxOf(num1, num2))
            .shuffled()
    }

    private fun resetData() {
        personAdapter?.list = initialData
    }

}
