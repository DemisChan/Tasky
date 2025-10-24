import com.dmd.tasky.feature.auth.domain.AuthRepository
import com.dmd.tasky.feature.auth.domain.model.LoginResult
import com.dmd.tasky.feature.auth.domain.model.RegisterResult
import kotlinx.coroutines.delay

class FakeAuthRepository : AuthRepository {

    var registerResult: RegisterResult = RegisterResult.Success

    override suspend fun register(
        fullName: String,
        email: String,
        password: String
    ): RegisterResult {
        delay(500)
        return registerResult
    }

    override suspend fun login(
        email: String,
        password: String
    ): LoginResult {
        throw UnsupportedOperationException("Not needed for register tests")
    }
}