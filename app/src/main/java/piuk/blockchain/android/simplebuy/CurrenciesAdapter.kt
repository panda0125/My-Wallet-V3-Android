package piuk.blockchain.android.simplebuy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blockchain.componentlib.viewextensions.gone
import com.blockchain.componentlib.viewextensions.visible
import info.blockchain.balance.FiatCurrency
import kotlin.properties.Delegates
import piuk.blockchain.android.databinding.CurrencySelectionItemBinding

class CurrenciesAdapter(
    private val showSectionDivider: Boolean = false,
    private val onChecked: (FiatCurrency) -> Unit
) : RecyclerView.Adapter<CurrenciesAdapter.CurrenciesViewHolder>() {

    var items: List<FiatCurrency> by Delegates.observable(emptyList()) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            notifyDataSetChanged()
        }
    }

    class CurrenciesViewHolder(binding: CurrencySelectionItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val currencyInfo = binding.currencyInfo
        val rootView: ViewGroup = binding.rootView
        val cellDivider: View = binding.currencyDivider
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrenciesViewHolder =
        CurrenciesViewHolder(
            CurrencySelectionItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CurrenciesViewHolder, position: Int) {
        with(holder) {
            val item = items[position]

            currencyInfo.apply {
                primaryText = item.name
                secondaryText = item.symbol
                onClick = { onChecked(item) }
            }

            when {
                position == items.size - 1 && showSectionDivider -> {
                    cellDivider.gone()
                }
                position != items.size -> {
                    cellDivider.visible()
                }
                else -> {
                    cellDivider.gone()
                }
            }
        }
    }
}
