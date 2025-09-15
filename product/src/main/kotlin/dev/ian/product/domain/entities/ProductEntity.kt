package dev.ian.product.domain.entities

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(
    name = "product",
)
class ProductEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false)
    var name: String,
    @Column(nullable = false)
    var price: BigDecimal
)
