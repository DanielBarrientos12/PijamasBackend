package com.neosoft.pijamasbakend.models;

import java.util.List;

public record TransactionSearchResponse(List<TransactionResponse.Data> data) {
}
