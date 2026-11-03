
package com.lumaledger.service;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(Long id) {
        super("Transaction not found with ID: " + id);
    }
}
