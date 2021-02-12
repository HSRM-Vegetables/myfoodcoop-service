package de.hsrm.vegetables.my_food_coop_service.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


public class OffsetLimit implements Pageable {

    private final long offset;
    private final int limit;

    /**
     * Creates a new {@link OffsetLimit}
     *
     * @param offset Zero-based offset
     * @param limit  The number of elements to be returned
     */
    public OffsetLimit(long offset, int limit) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must not be negative");
        }

        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be greater 0");
        }

        this.offset = offset;
        this.limit = limit;
    }

    @Override
    public int getPageNumber() {
        return (int)(offset / limit);
    }

    @Override
    public int getPageSize() {
        return limit;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return Sort.unsorted();
    }

    @Override
    public Pageable next() {
        return new OffsetLimit(getOffset() + getPageSize(), getPageSize());
    }

    public OffsetLimit previous() {
        return hasPrevious() ? new OffsetLimit(getOffset() - getPageSize(), getPageSize()) : this;
    }

    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ? previous() : first();
    }

    @Override
    public Pageable first() {
        return new OffsetLimit(0, getPageSize());
    }

    @Override
    public boolean hasPrevious() {
        return offset > limit;
    }
}
