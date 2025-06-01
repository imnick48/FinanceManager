package com.finManager.FinanceManager.Repository;


import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.finManager.FinanceManager.models.Transaction;
import com.finManager.FinanceManager.models.User;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserOrderByCreatedAtDesc(User user);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = ?1 AND t.type = 'INCOME'")
    BigDecimal getTotalIncomeByUser(User user);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = ?1 AND t.type = 'EXPENSE'")
    BigDecimal getTotalExpenseByUser(User user);
}
