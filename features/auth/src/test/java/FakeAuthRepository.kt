import com.dmd.tasky.core.domain.util.EmptyResult
import com.dmd.tasky.core.domain.util.Result
import com.dmd.tasky.features.auth.domain.AuthRepository
import com.dmd.tasky.features.auth.domain.model.AuthError
import com.dmd.tasky.features.auth.domain.model.LoginResult
import com.dmd.tasky.features.auth.domain.model.RegisterResult
import kotlinx.coroutines.delay

class FakeAuthRepository : AuthRepository {

    var registerResult: RegisterResult = Result.Success(Unit)



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

    override suspend fun logout(): EmptyResult<AuthError> {
        return Result.Success(Unit)
    }
}