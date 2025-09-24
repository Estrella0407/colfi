import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.colfi.data.repository.AuthRepository
import com.example.colfi.data.repository.CartRepository
import com.example.colfi.data.repository.OrdersRepository
import com.example.colfi.ui.viewmodel.CheckoutViewModel

class CheckoutViewModelFactory(
    private val cartRepository: CartRepository,
    private val authRepository: AuthRepository,
    private val ordersRepository: OrdersRepository = OrdersRepository()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CheckoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CheckoutViewModel(ordersRepository, cartRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
