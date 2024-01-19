package de.zonlykroks.p2p4all.util;

import java.util.concurrent.CompletableFuture;

public record GoleProcess(Process goleProcess, CompletableFuture<Void> associatedCompletableFuture) {
}
