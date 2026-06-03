package com.example.budgettracker.data.db

import androidx.room.TypeConverter
import com.example.budgettracker.data.entity.Cadence
import com.example.budgettracker.data.entity.Kind

/** Room type converters for the enum columns (stored as their .name). */
class Converters {
    @TypeConverter fun kindToString(kind: Kind): String = kind.name
    @TypeConverter fun stringToKind(value: String): Kind = Kind.valueOf(value)

    @TypeConverter fun cadenceToString(cadence: Cadence): String = cadence.name
    @TypeConverter fun stringToCadence(value: String): Cadence = Cadence.valueOf(value)
}
