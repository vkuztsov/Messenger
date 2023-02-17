CREATE TABLE "users" (
	"id" serial NOT NULL,
	"username" varchar(32),
	"first_name" varchar(32) NOT NULL,
	"last_name" varchar(32),
	"email" varchar(52) NOT NULL,
	"email_confirmed" BOOLEAN NOT NULL,
	"password" varchar(64) NOT NULL,
	"totp_secret" varchar(16),
	"profile_photo_url" varchar(255) NOT NULL,
	"user_pubkey" varchar(128) NOT NULL,
	"registration_date" TIMESTAMP NOT NULL,
	"registration_ip" varchar(15) NOT NULL,
	CONSTRAINT "users_pk" PRIMARY KEY ("id")
) WITH (
  OIDS=FALSE
);



CREATE TABLE "auth_keys" (
	"user_id" integer NOT NULL,
	"initial_key" varchar(12) NOT NULL,
	"auth_key" varchar(64) NOT NULL,
	"user_ip" varchar(15) NOT NULL,
	"last_visit" TIMESTAMP NOT NULL
) WITH (
  OIDS=FALSE
);



CREATE TABLE "chats" (
	"id" serial NOT NULL,
	"chat_owner_id" integer NOT NULL UNIQUE,
	"chat_name" varchar(32),
	"group" BOOLEAN NOT NULL,
	CONSTRAINT "chats_pk" PRIMARY KEY ("id")
) WITH (
  OIDS=FALSE
);



CREATE TABLE "contacts" (
	"user_id" integer NOT NULL UNIQUE,
	"contact_id" integer NOT NULL UNIQUE
) WITH (
  OIDS=FALSE
);



CREATE TABLE "messages" (
	"id" serial NOT NULL,
	"sender_id" integer NOT NULL UNIQUE,
	"chat_id" integer NOT NULL,
	"message" varchar(300) NOT NULL,
	"attach_url" varchar(255) NOT NULL,
	"created_at" TIMESTAMP NOT NULL,
	"readed" BOOLEAN NOT NULL,
	CONSTRAINT "messages_pk" PRIMARY KEY ("id")
) WITH (
  OIDS=FALSE
);



CREATE TABLE "chat_members" (
	"chat_id" integer NOT NULL,
	"user_id" integer NOT NULL,
	"admin" BOOLEAN NOT NULL
) WITH (
  OIDS=FALSE
);



CREATE TABLE "email_confirmation_codes" (
	"user_id" integer NOT NULL,
	"code" varchar(12) NOT NULL
) WITH (
  OIDS=FALSE
);

