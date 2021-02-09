package de.hsrm.vegetables.service.repositories;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.Serial;
import java.io.Serializable;


public class OffsetLimit implements Pageable, Serializable {

    @Serial
    private static final long serialVersionUID = -25822477129613575L;

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

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof OffsetLimit)) {
            return false;
        }

        OffsetLimit that = (OffsetLimit) other;

        return new EqualsBuilder()
                .append(limit, that.limit)
                .append(offset, that.offset)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(limit)
                .append(offset)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("limit", limit)
                .append("offset", offset)
                .toString();
    }
}
