package io.vepo.kafka.tool.controls.builders;

import java.util.List;
import java.util.function.BiConsumer;

public interface ResizePolicy {

    class DistributeResizePolicy implements ResizePolicy {

        private final int weight;

        public DistributeResizePolicy(int weight) {
            this.weight = weight;
        }

        int weight() {
            return weight;
        }

    }

    class FitContentResizePolicy implements ResizePolicy {

        private final int minWidth;
        private final int maxWidth;

        FitContentResizePolicy(int minWidth, int maxWidth) {
            this.minWidth = minWidth;
            this.maxWidth = maxWidth;
        }

        int maxWidth() {
            return maxWidth;
        }

        int minWidth() {
            return minWidth;
        }

    }

    class FixedSizeResizePolicy implements ResizePolicy {

        private final int size;
        private int penalty;

        public FixedSizeResizePolicy(int size) {
            this.size = size;
            this.penalty = 0;
        }

        public int getPenalty() {
            return penalty;
        }

        int penalty() {
            return penalty;
        }

        protected void setPenalty(int penalty) {
            this.penalty = penalty;
        }

        int size() {
            return size;
        }

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
            if (policy instanceof FixedSizeResizePolicy fixed) {
                fn.accept(i, (double) fixed.size);
            } else if (policy instanceof DistributeResizePolicy distribute) {
                fn.accept(i, Math.max(128, ((distribute.weight * distributable) / distibutionFactor)));
            }
        }
    }

    static ResizePolicy fitContent(int minWidth, int maxWidth) {
        return new FitContentResizePolicy(minWidth, maxWidth);
    }

    static ResizePolicy fixedSize(int size) {
        return new FixedSizeResizePolicy(size);
    }

    static ResizePolicy grow(int weight) {
        return new DistributeResizePolicy(weight);
    }

}
