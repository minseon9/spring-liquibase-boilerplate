package dev.ian.order.domain.entities


import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "order",
)
class OrderEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "product_id", nullable = false)
    val productId: Long,
    @Column(nullable = false)
    val count: Int,
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
