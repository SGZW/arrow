/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.arrow.vector.ipc.message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.VarCharVector;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/** Benchmarks for {@link ArrowRecordBatch}. */
@State(Scope.Benchmark)
public class ArrowRecordBatchBenchmarks {
  // checkstyle:off: MissingJavadocMethod

  private static final int VECTOR_CAPACITY = 16 * 1024;

  private static final int VECTOR_LENGTH = 1024;

  private static final int ALLOCATOR_CAPACITY = 1024 * 1024;

  private BufferAllocator allocator;

  private VarCharVector vector;

  private List<ArrowFieldNode> nodes;

  /** Setup benchmarks. */
  @Setup
  public void prepare() {
    allocator = new RootAllocator(ALLOCATOR_CAPACITY);
    vector = new VarCharVector("vector", allocator);
    vector.allocateNew(VECTOR_CAPACITY, VECTOR_LENGTH);

    nodes = new ArrayList<>();
    nodes.add(new ArrowFieldNode(VECTOR_LENGTH, 0));
    nodes.add(new ArrowFieldNode(VECTOR_LENGTH, 0));
  }

  /** Tear down benchmarks. */
  @TearDown
  public void tearDown() {
    vector.close();
    allocator.close();
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public long createAndGetLength() {
    try (ArrowRecordBatch batch =
        new ArrowRecordBatch(VECTOR_LENGTH, nodes, vector.getFieldBuffers())) {
      return batch.computeBodyLength();
    }
  }

  public static void main(String[] args) throws RunnerException {
    Options opt =
        new OptionsBuilder()
            .include(ArrowRecordBatchBenchmarks.class.getSimpleName())
            .forks(1)
            .build();

    new Runner(opt).run();
  }
  // checkstyle:on: MissingJavadocMethod
}