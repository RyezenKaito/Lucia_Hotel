USE LuciaHT;
GO

-- Find the constraint name
DECLARE @ConstraintName NVARCHAR(200);
SELECT @ConstraintName = name
FROM sys.check_constraints
WHERE parent_object_id = OBJECT_ID('NV') AND definition LIKE '%trinhDo%';

IF @ConstraintName IS NOT NULL
BEGIN
    DECLARE @SQL NVARCHAR(MAX) = 'ALTER TABLE NV DROP CONSTRAINT ' + @ConstraintName;
    EXEC sp_executesql @SQL;
END
GO

-- Update the existing values so we can add the constraint
UPDATE NV SET trinhDo = 'SAU_DAIHOC' WHERE trinhDo = 'TREN_DAIHOC';
UPDATE NV SET trinhDo = 'TRUNGCAP' WHERE trinhDo = 'THCS';
GO

-- Add the new constraint
ALTER TABLE NV ADD CONSTRAINT CK_NV_trinhDo
    CHECK (trinhDo IN (N'THPT', N'TRUNGCAP', N'CAODANG', N'DAIHOC', N'SAU_DAIHOC'));
GO
