package com.coding.challenge.infrastructure.common;

import java.util.List;
import java.util.Set;

public record ValidTransactionTypeProps(Set<String> allowedTypes){}