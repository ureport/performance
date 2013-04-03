SELECT ""poll_poll"".""id"", ""poll_poll"".""name"", ""poll_poll"".""question"", ""poll_poll"".""user_id"", ""poll_poll"".""start_date"", ""poll_poll"".""end_date"", ""poll_poll"".""type"", ""poll_poll"".""default_response"", ""poll_poll"".""response_type"" 

FROM ""poll_poll"" 

WHERE (NOT (""poll_poll"".""start_date"" IS NULL 
           AND (""poll_poll"".""id"" 
                IN (SELECT U0.""id"" FROM ""poll_poll"" U0 LEFT OUTER JOIN ""poll_poll_contacts"" U1 ON (U0.""id"" = U1.""poll_id"") LEFT OUTER JOIN ""rapidsms_contact"" U2 ON (U1.""contact_id"" = U2.""id"") WHERE (U2.""id"" IS NULL AND U0.""id"" IS NOT NULL)) AND ""poll_poll"".""id"" IS NOT NULL)) AND NOT (""poll_poll"".""id"" IN (297, 296, 349, 350))) ORDER BY ""poll_poll"".""start_date"" DESC LIMIT 10",,,,,,,,,""
