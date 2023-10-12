select * from notice;

INSERT INTO MENU_AUTHORITY (MEMBER_TYPE, MENU_ID)
VALUES ('3', '3000');

SELECT * FROM MENU_AUTHORITY;

INSERT INTO MEMBER (MEMBER_NO, MEMBER_ID, MEMBER_TYPE, MENU_ID, MEMBER_NAME, MEMBER_PW, MEMBER_SEX, MEMBER_HP, MEMBER_EMAIL, MEMBER_REGDATE, MEMBER_LOC, MEMBER_BIRTH, STATUS_YN)
VALUES (member_SEQ.NEXTVAL ,'admin','3','3000','관리자','!1234','여','01033490326','JOBSTARTUP@TEST.COM',SYSTIMESTAMP, '서울특별시 강남구 에스코빌딩 6층', '20000918', 'Y');

COMMIT;

/*컬럼 값 수정*/
ALTER TABLE QUESTION MODIFY Q_CONTENT CLOB;

ALTER TABLE QUESTION DROP COLUMN Q_CONTENT;

/*컬럼 추가*/
ALTER TABLE QUESTION ADD Q_CONTENT CLOB NOT NULL;

ALTER TABLE QUESTION ADD Q_TYPE VARCHAR2(50) NOT NULL;

ALTER TABLE QUESTION ADD Q_CATEGORY VARCHAR2(200) NOT NULL;

/*제약 조건(fk) 추가*/
alter table QUESTION add foreign key(COMPANY_NO) references COMPANY(COMPANY_NO);