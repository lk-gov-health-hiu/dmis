  -- Preview first
  SELECT COUNT(*) AS records_to_update
  FROM documenthistory dh
  JOIN webuser wu ON dh.TOUSER_ID = wu.ID
  WHERE dh.TOUSER_ID IS NOT NULL
    AND dh.TOINSTITUTION_ID IS NULL
    AND dh.HISTORYTYPE = 'Letter_Copy_or_Forward';

  -- Then update
  UPDATE documenthistory dh
  JOIN webuser wu ON dh.TOUSER_ID = wu.ID
  SET dh.TOINSTITUTION_ID = wu.INSTITUTION_ID
  WHERE dh.TOUSER_ID IS NOT NULL
    AND dh.TOINSTITUTION_ID IS NULL
    AND dh.HISTORYTYPE = 'Letter_Copy_or_Forward';