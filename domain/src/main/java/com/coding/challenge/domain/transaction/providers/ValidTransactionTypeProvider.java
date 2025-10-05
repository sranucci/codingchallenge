package com.coding.challenge.domain.transaction.providers;

import java.util.List;
import java.util.Set;

public interface ValidTransactionTypeProvider {
    Set<String> getValidTransactionTypes();
}
