/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.prestosql.orc.metadata;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

public class ColumnEncoding
{
    public enum ColumnEncodingKind
    {
        DIRECT,
        DICTIONARY,
        DIRECT_V2,
        DICTIONARY_V2,
        DWRF_DIRECT,
        DWRF_MAP_FLAT,
    }

    public static final int DEFAULT_SEQUENCE_ID = 0;

    private final ColumnEncodingKind columnEncodingKind;
    private final int dictionarySize;

    // DWRF supports the concept of sequences.
    // A column can be modeled as multiple sequences that are independently encoded.
    // For example, for a flat map column, each key will have a
    // separate value stream with its own column encoding.
    private final Optional<List<DwrfSequenceEncoding>> additionalSequenceEncodings;

    public ColumnEncoding(ColumnEncodingKind columnEncodingKind, int dictionarySize)
    {
        this(columnEncodingKind, dictionarySize, Optional.empty());
    }

    public ColumnEncoding(ColumnEncodingKind columnEncodingKind, int dictionarySize, Optional<List<DwrfSequenceEncoding>> additionalSequenceEncodings)
    {
        this.columnEncodingKind = requireNonNull(columnEncodingKind, "columnEncodingKind is null");
        this.dictionarySize = dictionarySize;
        this.additionalSequenceEncodings = additionalSequenceEncodings;
    }

    public ColumnEncodingKind getColumnEncodingKind()
    {
        return columnEncodingKind;
    }

    public int getDictionarySize()
    {
        return dictionarySize;
    }

    public Optional<List<DwrfSequenceEncoding>> getAdditionalSequenceEncodings()
    {
        return additionalSequenceEncodings;
    }

    public ColumnEncoding getColumnEncoding(int sequence)
    {
        if (sequence == 0) {
            return this;
        }

        checkState(
                additionalSequenceEncodings.isPresent(),
                "Got non-zero sequence: %d, but there are no additional sequence encodings: %s", sequence, this);

        return additionalSequenceEncodings.get().get(sequence - 1).getValueEncoding();
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
                .add("columnEncodingKind", columnEncodingKind)
                .add("dictionarySize", dictionarySize)
                .add("additionalSequenceEncodings", additionalSequenceEncodings)
                .toString();
    }
}
