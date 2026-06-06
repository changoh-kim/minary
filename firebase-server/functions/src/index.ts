import {onMessagePublished} from "firebase-functions/v2/pubsub";
import {defineSecret} from "firebase-functions/params";
import fetch from "node-fetch";
import {onCall, HttpsError} from "firebase-functions/v2/https";
import {onObjectFinalized} from "firebase-functions/v2/storage";
import {setGlobalOptions} from "firebase-functions/v2";
import * as admin from "firebase-admin";
import * as logger from "firebase-functions/logger";
import {getDownloadURL} from "firebase-admin/storage";

if (!admin.apps.length) {
  admin.initializeApp();
}

setGlobalOptions({
  region: "asia-northeast3",
  maxInstances: 3,
  timeoutSeconds: 60,
  invoker: "public",
  enforceAppCheck: false,
});

interface CreateAccountRequest {
  email?: string;
  password?: string;
  name?: string;
  gender?: string;
  birthday?: string;
  address?: string;
  phoneNumber?: string;
  joinedAt?: number;
}

// firebase 서비스 이용가능 확인
async function assertServiceAvailable(): Promise<void> {
  const doc = await admin
    .firestore()
    .collection("system")
    .doc("service_control")
    .get();

  if (!doc.exists) {
    throw new HttpsError(
      "unavailable",
      "Service control document missing"
    );
  }

  const data = doc.data();
  if (data?.isMaintenanceMode === true) {
    throw new HttpsError(
      "unavailable",
      data.maintenanceReason || "The system is currently undergoing maintenance for a better service"
    );
  }
}

// 회원가입 이메일 중복 확인
export const checkEmailAvailability = onCall(
  {
    enforceAppCheck: false,
    maxInstances: 3,
    memory: "256MiB",
    timeoutSeconds: 60,
  },
  async (request) => {
    await assertServiceAvailable();

    const data = request.data as { email?: string };
    if (!data.email) {
      throw new HttpsError("invalid-argument", "Email is required.");
    }
    try {
      await admin.auth().getUserByEmail(data.email);
      return {isAvailable: false};
    } catch (error: unknown) {
      if (
        typeof error === "object" &&
        error !== null &&
        "code" in error &&
        (error as { code: string }).code === "auth/user-not-found"
      ) {
        return {isAvailable: true};
      }
      const message = error instanceof Error ? error.message : "Unknown error";
      throw new HttpsError("internal", message);
    }
  }
);

// 회원가입 및 사용자 데이터 생성
export const createAccountAndUserData = onCall(
  {
    enforceAppCheck: false,
    maxInstances: 5,
    memory: "512MiB",
    timeoutSeconds: 60,
  },
  async (request) => {
    await assertServiceAvailable();

    const data = request.data as CreateAccountRequest;

    if (!data.email || !data.password || !data.name) {
      logger.error("Missing required information:", data);
      throw new HttpsError(
        "invalid-argument",
        "Email, password, and name are required."
      );
    }

    try {
      const userRecord = await admin.auth().createUser({
        email: data.email,
        password: data.password,
        displayName: data.name,
      });

      const uid = userRecord.uid;
      const db = admin.firestore();
      const batch = db.batch();

      const userRef = db.collection("users").doc(uid);
      const profileRef = userRef.collection("profile").doc("userProfile");
      const joinedAt = data.joinedAt || Date.now();
      batch.set(profileRef, {
        uid: uid,
        email: data.email,
        name: data.name,
        gender: data.gender || null,
        birthday: data.birthday || null,
        address: data.address || null,
        phoneNumber: data.phoneNumber || null,
        joinedAt: joinedAt,
        lastModifiedAt: joinedAt,
      });

      const userSettingsRef = userRef.collection("settings").doc("userSettings");
      batch.set(userSettingsRef, {
        theme: "SYSTEM",
        diarySyncEnabled: false,
        lastModifiedAt: joinedAt,
      });

      await batch.commit();

      logger.info(`Created new users and profiles and settings: ${uid}`);
      return {success: true, uid: uid};
    } catch (error) {
      if (error instanceof Error) {
        logger.error("Error during account creation:", error.message);
        throw new HttpsError("internal", error.message);
      }
      throw new HttpsError("internal", "An unknown error occurred.");
    }
  }
);

// 회원탈퇴 및 사용자 데이터 삭제
export const deleteAccountAndUserData = onCall(
  {
    enforceAppCheck: false,
    maxInstances: 3,
    memory: "512MiB",
    timeoutSeconds: 180,
  },
  async (request) => {
    await assertServiceAvailable();

    if (!request.auth) {
      throw new HttpsError(
        "unauthenticated",
        "The function must be called while authenticated."
      );
    }

    const uid = request.auth.uid;

    try {
      const db = admin.firestore();
      const userRef = db.collection("users").doc(uid);

      const deleteCollection =
        async (collectionRef: admin.firestore.CollectionReference) => {
          const snapshot = await collectionRef.get();
          const batch = db.batch();
          snapshot.forEach((doc) => batch.delete(doc.ref));
          await batch.commit();
        };

      // 1. user 하위 데이터 삭제 (Firestore)
      await deleteCollection(userRef.collection("diaries"));
      await deleteCollection(userRef.collection("profile"));
      await deleteCollection(userRef.collection("settings"));

      // 2. 프로필 사진 삭제 (Storage)
      try {
        const bucket = admin.storage().bucket();
        const profilePhotoFile = bucket.file(`profile_photos/${uid}.jpg`);
        await profilePhotoFile.delete({ignoreNotFound: true});
      } catch (error) {
        logger.error(`Error deleting profile photo for user ${uid}:`, error);
      }

      // 3. user 문서 삭제 (Firestore)
      await userRef.delete();

      // 4. 계정 삭제 (Auth)
      await admin.auth().deleteUser(uid);

      logger.info(`All data and account deleted for user: ${uid}`);
      return {success: true};
    } catch (error) {
      if (error instanceof Error) {
        logger.error(`Error during deletion for user ${uid}:`, error.message);
        throw new HttpsError("internal", error.message);
      }
      throw new HttpsError("internal", "An unknown error occurred.");
    }
  }
);

// firebase storage에 프로필 사진을 업로드하면 발생하는 Event 함수
export const onProfilePhotoUploaded = onObjectFinalized(
  {
    maxInstances: 2,
    memory: "256MiB",
    timeoutSeconds: 60,
  },
  async (event) => {
    const filePath = event.data.name; // 예: profile_photos/UID.jpg

    if (!filePath.startsWith("profile_photos/")) {
      return;
    }

    const uid = filePath.split("/")[1].split(".")[0];
    if (!uid) {
      logger.error("Failed to extract UID from path:", filePath);
      return;
    }

    try {
      const bucket = admin.storage().bucket(event.data.bucket);
      const file = bucket.file(filePath);

      const downloadUrl = await getDownloadURL(file);

      // Metadata에서 lastModifiedAt 추출 (문자열 -> 숫자 변환)
      const customMetadata = event.data.metadata || {};
      const lastModifiedAt = customMetadata.lastModifiedAt ?
        parseInt(customMetadata.lastModifiedAt) :
        (event.data.updated ?
          new Date(event.data.updated).getTime() : Date.now());

      const db = admin.firestore();
      const profileRef = db
        .collection("users")
        .doc(uid)
        .collection("profile")
        .doc("userProfile");

      await profileRef.update({
        profilePhotoUrl: downloadUrl,
        lastModifiedAt,
      });

      logger.info(`Profile photo updated via trigger for user: ${uid} ` +
          `with timestamp: ${lastModifiedAt}`);
    } catch (error) {
      logger.error(`Error updating profile photo for user ${uid}:`, error);
    }
  }
);

const slackWebhookSecret = defineSecret("SLACK_WEBHOOK_URL");
const PROJECT_ID = process.env.GCLOUD_PROJECT || "";

// Slack 알림 메세지 전송
async function sendSlackAlert(
  webhookUrl: string,
  costAmount: number,
  budgetAmount: number,
  thresholdPercent: number,
  actions: string[]
): Promise<void> {
  const percentage = Math.round((costAmount / budgetAmount) * 100);
  const emoji =
    thresholdPercent >= 100 ? "🚨" :
      thresholdPercent >= 90 ? "⚠️" :
        thresholdPercent >= 80 ? "🔶" : "📊";
  const color =
    thresholdPercent >= 100 ? "#FF0000" :
      thresholdPercent >= 90 ? "#FF6600" :
        thresholdPercent >= 80 ? "#FF8C00" : "#FFC107";

  const actionBlock = actions.length > 0 ? [{
    type: "section",
    text: {
      type: "mrkdwn",
      text: `*조치 내용:*\n${actions.map((a) => `• ${a}`).join("\n")}`,
    },
  }] : [];

  const message = {
    attachments: [{
      color,
      blocks: [
        {
          type: "header",
          text: {
            type: "plain_text",
            text: `${emoji} Minary Firebase 예산 알림 — ${thresholdPercent}% 도달`,
          },
        },
        {
          type: "section",
          fields: [
            {type: "mrkdwn", text: `*현재 사용액:*\n$${costAmount.toFixed(2)}`},
            {type: "mrkdwn", text: `*월 예산:*\n$${budgetAmount.toFixed(2)}`},
            {type: "mrkdwn", text: `*사용률:*\n${percentage}%`},
            {
              type: "mrkdwn",
              text: `*서비스 상태:*\n${thresholdPercent >= 100 ? "🔴 기능 제한 중" : "🟢 정상"}`,
            },
          ],
        },
        ...actionBlock,
        {
          type: "context",
          elements: [{
            type: "mrkdwn",
            text: "⏱ Budget Alert는 실제 비용 대비 최대 수 시간 지연될 수 있습니다. " +
              "현재 실제 비용이 표시된 수치보다 높을 수 있습니다. " +
              "<https://console.cloud.google.com/billing|Billing Console에서 확인>",
          }],
        },
      ],
    }],
  };

  await fetch(webhookUrl, {
    method: "POST",
    headers: {"Content-Type": "application/json"},
    body: JSON.stringify(message),
  });
}

// 기능 제한 활성화
async function enableKillSwitch(
  thresholdPercent: number
): Promise<void> {
  const reason = "The system is currently undergoing maintenance for a better service. Please try again later.";
  await Promise.all([
    admin
      .firestore()
      .collection("system")
      .doc("service_control")
      .set({
        isMaintenanceMode: true,
        maintenanceReason: reason,
        lastModifiedAt: admin.firestore.FieldValue.serverTimestamp(),
      }, {
        merge: true,
      }),
    updateRemoteConfig(true, reason),
  ]);
  logger.info(`월 예산 ${thresholdPercent}% 초과로 기능이 제한되었습니다.`);
}

// 기능 제한 비활성화
export const disableKillSwitch = onCall(
  {enforceAppCheck: false},
  async (request) => {
    if (!request.auth) {
      throw new HttpsError(
        "unauthenticated",
        "Authentication required"
      );
    }

    const user = await admin.auth().getUser(
      request.auth.uid
    );

    if (!user.customClaims?.admin) {
      throw new HttpsError(
        "permission-denied",
        "Admin only"
      );
    }

    await Promise.all([
      admin
        .firestore()
        .collection("system")
        .doc("service_control")
        .set({
          isMaintenanceMode: false,
          maintenanceReason: "",
          lastModifiedAt: admin.firestore.FieldValue.serverTimestamp(),
        }, {merge: true}),
      updateRemoteConfig(false, ""),
    ]);

    return {success: true};
  }
);

// Remote Config 값을 업데이트합니다.
async function updateRemoteConfig(isMaintenance: boolean, reason: string): Promise<void> {
  try {
    const config = admin.remoteConfig();
    const template = await config.getTemplate();

    const isEnabled = !isMaintenance;
    const now = Date.now().toString(); // 밀리초 단위 타임스탬프

    // 수정 시간 기록 (NUMBER 타입)
    template.parameters["last_modified_at"] = {
      defaultValue: {value: now},
      valueType: "NUMBER",
    };

    // 점검 모드 및 사유
    template.parameters["is_maintenance_mode"] = {
      defaultValue: {value: isMaintenance.toString()},
      valueType: "BOOLEAN",
    };

    template.parameters["maintenance_reason"] = {
      defaultValue: {value: reason},
      valueType: "STRING",
    };

    // 개별 기능 플래그 (isMaintenance의 반대값)
    template.parameters["user_data_sync_enabled"] = {
      defaultValue: {value: isEnabled.toString()},
      valueType: "BOOLEAN",
    };

    template.parameters["profile_photo_upload_enabled"] = {
      defaultValue: {value: isEnabled.toString()},
      valueType: "BOOLEAN",
    };

    template.parameters["diary_sync_enabled"] = {
      defaultValue: {value: isEnabled.toString()},
      valueType: "BOOLEAN",
    };

    template.parameters["ai_enabled"] = {
      defaultValue: {value: isEnabled.toString()},
      valueType: "BOOLEAN",
    };

    await config.publishTemplate(template);
    logger.info(`Remote Config updated: is_maintenance_mode=${isMaintenance}, last_modified_at=${now}`);
  } catch (error) {
    logger.error("Error updating Remote Config:", error);
  }
}

// 결제 정보 연결 해제
async function disconnectBilling(): Promise<void> {
  const {CloudBillingClient} = await import("@google-cloud/billing");
  const billingClient = new CloudBillingClient();

  await billingClient.updateProjectBillingInfo({
    name: `projects/${PROJECT_ID}`,
    projectBillingInfo: {
      billingAccountName: "", // 빈 문자열 = Billing 연결 해제
    },
  });

  logger.error(
    `⚠️ Billing DISCONNECTED for project: ${PROJECT_ID}. ` +
    "All Firebase services are now suspended."
  );
}

// 예산 알림 서비스 핸들러
export const handleBudgetAlert = onMessagePublished(
  {
    topic: "billing-budget-alerts",
    region: "asia-northeast3",
    secrets: [slackWebhookSecret],
    maxInstances: 2,
    memory: "256MiB",
    timeoutSeconds: 120,
  },
  async (event) => {
    // Pub/Sub 메시지 파싱
    const messageData = event.data.message.json as {
      budgetDisplayName: string;
      costAmount: number;
      budgetAmount: number;
      currencyCode: string;
      costIntervalStart: string;
    };

    const {costAmount, budgetAmount} = messageData;
    const thresholdPercent = Math.round((costAmount / budgetAmount) * 100);

    logger.info(
      `Budget alert: ${thresholdPercent}% ` +
      `(cost: ${costAmount}, budget: ${budgetAmount})`
    );

    const slackWebhookUrl = slackWebhookSecret.value();


    // 50% ~ 89% -> Slack + Email 알림
    if (thresholdPercent >= 50 && thresholdPercent < 90) {
      await Promise.all([
        sendSlackAlert(
          slackWebhookUrl,
          costAmount,
          budgetAmount,
          thresholdPercent,
          []
        ),
      ]);
      logger.info(`Notification sent for ${thresholdPercent}%`);
      return;
    }

    // 90% ~ 99% -> Slack + Email 알림
    if (thresholdPercent >= 90 && thresholdPercent < 100) {
      const actions = [
        "⚠️ Budget Alert 지연 특성상 현재 실제 비용이 100%를 초과했을 수 있습니다.",
        "Billing Console에서 실시간 비용을 즉시 확인하세요.",
      ];
      await Promise.all([
        sendSlackAlert(
          slackWebhookUrl,
          costAmount,
          budgetAmount,
          thresholdPercent,
          actions),
      ]);
      logger.warn("90% threshold reached — pre-emptive alert sent");
      return;
    }

    // 100% ~ 119% -> Slack + Email 알림 (경고), 기능 제한 활성화
    if (thresholdPercent >= 100 && thresholdPercent < 120) {
      const actions = [
        "Firestore Kill Switch 활성화",
        "Cloud Functions 요청 차단",
        "동기화 기능 비활성화",
        "이미지 업로드 비활성화",
      ];
      await Promise.all([
        sendSlackAlert(
          slackWebhookUrl,
          costAmount,
          budgetAmount,
          thresholdPercent,
          actions
        ),
        enableKillSwitch(thresholdPercent),
      ]);
      logger.warn(
        `Services restricted via Firestore Kill Switch at ${thresholdPercent}%`
      );
      return;
    }

    // 120% 이상 -> Slack + Email 알림 + Billing Disconnect(Firebase 서비스 중단)
    if (thresholdPercent >= 120) {
      const actions = [
        "Billing 계정 연결 해제 완료",
        "모든 Firebase 서비스 즉시 중단",
        "Firestore 데이터·Storage 파일은 보존됨",
        "복구: GCP Console에서 Billing 재연결 필요",
      ];

      try {
        await disconnectBilling();
      } catch (billingError) {
        logger.error("Billing disconnect failed:", billingError);
        actions.push("⚠️ Billing 연결 해제 실패 — 수동 처리 필요");
      }

      await Promise.all([
        sendSlackAlert(
          slackWebhookUrl,
          costAmount,
          budgetAmount,
          thresholdPercent,
          actions
        ),
      ]);
      logger.error(`Billing disconnected at ${thresholdPercent}%`);
    }
  }
);

// 서비스 컨트롤 데이터 초기화 함수
export const initializeServiceControl = onCall(
  {enforceAppCheck: false},
  async (request) => {
    if (!request.auth) {
      throw new HttpsError(
        "unauthenticated",
        "Authentication required"
      );
    }

    const user = await admin.auth().getUser(request.auth.uid);
    if (!user.customClaims?.admin) {
      throw new HttpsError(
        "permission-denied",
        "Admin only"
      );
    }

    await admin
      .firestore()
      .collection("system")
      .doc("service_control")
      .set({
        isMaintenanceMode: false,
        maintenanceReason: "",
        userDataSyncEnabled: true,
        profilePhotoUploadEnabled: true,
        diarySyncEnabled: true,
        lastModifiedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

    return {
      success: true,
    };
  }
);
