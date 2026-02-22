package com.example.data.repo

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension
import com.example.core.result.AppError
import com.example.core.result.AppResult
import com.example.data.db.ProductDao
import com.example.data.mappers.toDomain
import com.example.data.mappers.toEntity
import com.example.data.network.OpenFoodFactsApi
import com.example.data.network.toAppError
import com.example.domain.model.Product
import com.example.domain.repo.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import javax.inject.Inject

class OfflineFirstProductRepository @RequiresApi(Build.VERSION_CODES.O)
@Inject constructor(
    private val dao: ProductDao,
    private val api: OpenFoodFactsApi,
    private val clock: Clock
) : ProductRepository {

    override fun observeRecent(): Flow<List<Product>> =
        dao.observeRecent().map { list -> list.map { it.toDomain() } }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun refreshByBarcode(barcode: String): AppResult<Unit> {
        val normalized = barcode.trim()
        if (normalized.isBlank()) return AppResult.Error(AppError.Unknown("Empty barcode"))

        return try {
            val resp = api.getProduct(normalized)
            if (!resp.isSuccessful) {
                return AppResult.Error(AppError.Http(resp.code()))
            }

            val body = resp.body()
            val dto = body?.product
            val status = body?.status ?: 0

            // OFF: status == 1 means found
            // if no product => "not found"
            if (status != 1 || dto == null) {
                return AppResult.Error(AppError.Http(404))
            }

            val entity = dto.toEntity(nowEpochMs = clock.millis())
                ?: return AppResult.Error(AppError.Serialization)

            dao.upsert(entity)
            AppResult.Success(Unit)
        } catch (t: Throwable) {
            AppResult.Error(t.toAppError())
        }
    }
}