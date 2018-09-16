package com.bigneardranch.android.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.bigneardranch.android.criminalintent.database.CrimeBaseHelper;
import com.bigneardranch.android.criminalintent.database.CrimeCursorWrapper;
import com.bigneardranch.android.criminalintent.database.CrimeDbSchema.CrimeTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeLab {
    private static CrimeLab sCrimeLab;

    private SQLiteDatabase mDataBase;

    public void addCrime(Crime crime) {
        ContentValues values = getContentValues(crime);
        mDataBase.insert(CrimeTable.NAME, null, values);
    }

    public void deleteCrime(Crime crime) {
        mDataBase.delete(CrimeTable.NAME,
                CrimeTable.Cols.UUID + " = ?",
                new String[] { crime.getId().toString() });
    }

    public static CrimeLab get(Context context) {
        if (sCrimeLab == null)
            sCrimeLab = new CrimeLab(context);
        return sCrimeLab;
    }

    private CrimeLab(Context context) {
        Context appContext = context.getApplicationContext();
        mDataBase = new CrimeBaseHelper(appContext).getWritableDatabase();
    }

    public List<Crime> getCrimes() {
        List<Crime> crimes = new ArrayList<>();

        try (CrimeCursorWrapper cursor = queryCrimes(null, null)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        }

        return crimes;
    }

    public Crime getCrime(UUID id) {
        try (CrimeCursorWrapper cursor = queryCrimes(
                CrimeTable.Cols.UUID + " = ?",
                new String[]{id.toString()})) {
            if (cursor.getCount() == 0)
                return null;

            cursor.moveToFirst();
            return cursor.getCrime();
        }
    }

    public void updateCrime(Crime crime) {
        String uuidString = crime.getId().toString();
        ContentValues values = getContentValues(crime);

        mDataBase.update(CrimeTable.NAME, values,
                CrimeTable.Cols.UUID + " = ?",
                new String[] { uuidString });
    }

    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
        return new CrimeCursorWrapper(mDataBase.query(
                CrimeTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        ));
    }

    private ContentValues getContentValues(Crime crime) {
        ContentValues values = new ContentValues();
        values.put(CrimeTable.Cols.UUID, crime.getId().toString());
        values.put(CrimeTable.Cols.TITLE, crime.getTitle());
        values.put(CrimeTable.Cols.DATE, crime.getDate().getTime());
        values.put(CrimeTable.Cols.SOLVED, crime.isSolved() ? 1 : 0);

        return values;
    }
}
