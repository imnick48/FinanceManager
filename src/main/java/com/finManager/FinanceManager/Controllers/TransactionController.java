package com.finManager.FinanceManager.Controllers;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finManager.FinanceManager.Repository.TransactionRepository;
import com.finManager.FinanceManager.Repository.UserRepository;
import com.finManager.FinanceManager.models.Transaction;
import com.finManager.FinanceManager.models.User;

@RestController
@RequestMapping("/api")
public class TransactionController {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        String googleId = principal.getAttribute("sub");
        Optional<User> userOpt = userRepository.findByGoogleId(googleId);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("pictureUrl", user.getPictureUrl());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(@AuthenticationPrincipal OAuth2User principal) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        List<Transaction> transactions = transactionRepository.findByUserOrderByCreatedAtDesc(user);
        return ResponseEntity.ok(transactions);
    }
    
    @PostMapping("/transactions")
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction, @AuthenticationPrincipal OAuth2User principal) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        transaction.setUser(user);
        Transaction savedTransaction = transactionRepository.save(transaction);
        return ResponseEntity.ok(savedTransaction);
    }
    
    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id, @AuthenticationPrincipal OAuth2User principal) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        Optional<Transaction> transactionOpt = transactionRepository.findById(id);
        if (transactionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Transaction transaction = transactionOpt.get();
        if (!transaction.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        transactionRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/summary")
    public ResponseEntity<Map<String, BigDecimal>> getSummary(@AuthenticationPrincipal OAuth2User principal) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        BigDecimal totalIncome = transactionRepository.getTotalIncomeByUser(user);
        BigDecimal totalExpense = transactionRepository.getTotalExpenseByUser(user);
        
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;
        
        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpense", totalExpense);
        summary.put("balance", totalIncome.subtract(totalExpense));
        
        return ResponseEntity.ok(summary);
    }
    
    private User getCurrentUser(OAuth2User principal) {
        if (principal == null) {
            return null;
        }
        
        String googleId = principal.getAttribute("sub");
        return userRepository.findByGoogleId(googleId).orElse(null);
    }
}