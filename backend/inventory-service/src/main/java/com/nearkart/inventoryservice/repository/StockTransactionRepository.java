package com.nearkart.inventoryservice.repository;

import com.nearkart.inventoryservice.model.StockTransaction;
import com.nearkart.inventoryservice.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    List<StockTransaction> findByInventoryItemIdOrderByCreatedAtDesc(Long inventoryItemId);

    List<StockTransaction> findByTransactionTypeOrderByCreatedAtDesc(TransactionType type);

    List<StockTransaction> findByReferenceId(String referenceId);
}
