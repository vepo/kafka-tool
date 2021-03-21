package io.vepo.kt.ui;

import java.util.List;
import java.util.function.BiConsumer;

public interface ResizePolicy {

    static ResizePolicy fixedSize(int size) {
        return new FixedSizeResizePolicy(size);
    }

    class FixedSizeResizePolicy implements ResizePolicy {

        private final int size;
        private int penalty;

        public FixedSizeResizePolicy(int size) {
            this.size = size;
            this.penalty = 0;
        }
        
        protected void setPenalty(int penalty) {
            this.penalty = penalty;
        }
        
        public int getPenalty() {
            return penalty;
        }

    }

    class DistributeResizePolicy implements ResizePolicy {

        private final int weight;

        public DistributeResizePolicy(int weight) {
            this.weight = weight;
        }

    }

    static ResizePolicy grow(int weight) {
        return new DistributeResizePolicy(weight);
    }

    static void apply(List<ResizePolicy> resizePolicies, double totalWidth, BiConsumer<Integer, Double> fn) {
        double distributable = totalWidth
                - resizePolicies.size() + 1 // number of borders
                - resizePolicies.stream()
                                .filter(policy -> policy instanceof FixedSizeResizePolicy)
                                .mapToDouble(r -> (double) ((FixedSizeResizePolicy) r).size + (double) ((FixedSizeResizePolicy) r).penalty)
                                .sum();
        int distibutionFactor = (int) resizePolicies.stream()
                                                    .filter(policy -> policy instanceof DistributeResizePolicy)
                                                    .mapToInt(r -> ((DistributeResizePolicy) r).weight)
                                                    .sum();
        for (int i = 0; i < resizePolicies.size(); ++i) {
            var policy = resizePolicies.get(i);
            if (policy instanceof FixedSizeResizePolicy) {
                fn.accept(i, (double) ((FixedSizeResizePolicy) policy).size);
            } else if (policy instanceof DistributeResizePolicy) {
                fn.accept(i, Math.max(128,
                                      (((DistributeResizePolicy) policy).weight * distributable) / distibutionFactor));
            }
        }
    }

}
