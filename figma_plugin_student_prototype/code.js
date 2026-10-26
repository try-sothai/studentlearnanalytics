// SLAS Master Prototype Builder — Figma Plugin Code
// Inspects Figma document for Student, Teacher, and Admin frames and wires interactive prototype reactions for all 3 roles.

figma.showUI(__html__, { width: 480, height: 640 });

// Role-specific Screen Patterns
const PATTERNS = {
  student: {
    login: [/student\s*login/i, /login/i, /sign\s*in/i],
    dashboard: [/student\s*dashboard/i, /student\s*home/i, /home/i, /dashboard/i],
    courses: [/my\s*courses/i, /student\s*courses/i, /course\s*list/i, /courses/i],
    courseDetail: [/course\s*detail/i, /course_detail/i, /course_info/i],
    analytics: [/learning\s*analytics/i, /analytics\s*overview/i, /analytics/i],
    performanceDetails: [/performance\s*details/i, /overall\s*performance/i, /performance_detail/i],
    prediction: [/prediction/i, /risk/i, /prediction\s*&\s*risk/i, /prediction_detail/i],
    attendance: [/attendance/i, /attendance\s*details/i],
    assignments: [/assignments?/i],
    quizzes: [/quizz?es?/i],
    exams: [/exams?/i],
    gpa: [/gpa/i, /academic\s*progress/i, /gpa\s*progress/i],
    report: [/reports?/i, /learning\s*report/i, /report_preview/i],
    recommendations: [/recommendations?/i],
    notifications: [/notifications?/i],
    profile: [/student\s*profile/i, /my\s*profile/i, /profile/i],
    drawer: [/student\s*drawer/i, /drawer/i, /navigation\s*drawer/i]
  },
  teacher: {
    login: [/teacher\s*login/i],
    dashboard: [/teacher\s*dashboard/i, /teacher\s*home/i],
    studentList: [/student\s*list/i, /manage\s*students/i],
    studentDetail: [/student\s*detail/i],
    manageScores: [/manage\s*scores/i, /enter\s*scores/i, /scores/i],
    attendance: [/teacher\s*attendance/i, /class\s*attendance/i],
    classAnalytics: [/class\s*analytics/i],
    predictionRisk: [/teacher\s*prediction/i, /class\s*risk/i],
    recommendations: [/teacher\s*recommendations/i],
    reports: [/teacher\s*reports/i],
    evaluation: [/evaluation/i, /model\s*evaluation/i],
    courseManagement: [/teacher\s*courses/i, /manage\s*courses/i],
    notifications: [/teacher\s*notifications/i],
    profile: [/teacher\s*profile/i],
    drawer: [/teacher\s*drawer/i]
  },
  admin: {
    login: [/admin\s*login/i],
    dashboard: [/admin\s*dashboard/i, /admin\s*home/i],
    userManagement: [/user\s*management/i, /manage\s*users/i],
    teacherManagement: [/teacher\s*management/i, /manage\s*teachers/i],
    courseManagement: [/admin\s*course\s*management/i],
    systemStats: [/system\s*statistics/i, /system\s*stats/i],
    mlEvaluation: [/ml\s*model\s*evaluation/i, /ml\s*evaluation/i],
    reports: [/admin\s*reports/i],
    notifications: [/admin\s*notifications/i],
    systemSettings: [/system\s*settings/i],
    profile: [/admin\s*profile/i],
    changePassword: [/change\s*password/i]
  }
};

function matchesPattern(text, patterns) {
  if (!text) return false;
  return patterns.some(pattern => pattern.test(text));
}

function findFrames() {
  const matched = { student: {}, teacher: {}, admin: {} };

  function traverse(node) {
    if (node.type === "FRAME" || node.type === "COMPONENT" || node.type === "SECTION") {
      const name = node.name ? node.name.trim() : "";

      // Distinguish Role
      if (/admin/i.test(name)) {
        for (const [key, patterns] of Object.entries(PATTERNS.admin)) {
          if (!matched.admin[key] && matchesPattern(name, patterns)) {
            matched.admin[key] = { id: node.id, name: node.name, node: node };
          }
        }
      } else if (/teacher/i.test(name) || /class\s*analytics/i.test(name)) {
        for (const [key, patterns] of Object.entries(PATTERNS.teacher)) {
          if (!matched.teacher[key] && matchesPattern(name, patterns)) {
            matched.teacher[key] = { id: node.id, name: node.name, node: node };
          }
        }
      } else {
        for (const [key, patterns] of Object.entries(PATTERNS.student)) {
          if (!matched.student[key] && matchesPattern(name, patterns)) {
            matched.student[key] = { id: node.id, name: node.name, node: node };
          }
        }
      }
    }

    if ("children" in node) {
      for (const child of node.children) {
        traverse(child);
      }
    }
  }

  if (figma.root && figma.root.children) {
    for (const page of figma.root.children) traverse(page);
  } else if (figma.currentPage) {
    traverse(figma.currentPage);
  }

  return matched;
}

function findChildByNameOrText(parent, searchPatterns) {
  let found = null;
  function walk(node) {
    if (found) return;
    const nodeName = node.name || "";
    const nodeText = (node.type === "TEXT" && node.characters) ? node.characters : "";
    if (matchesPattern(nodeName, searchPatterns) || matchesPattern(nodeText, searchPatterns)) {
      found = node;
      return;
    }
    if ("children" in node) {
      for (const child of node.children) walk(child);
    }
  }
  walk(parent);
  return found;
}

function generateConnections(allFrames) {
  const connections = [];
  const missing = [];

  function addConnection(sourceNode, sourceName, triggerLabel, targetRole, targetKey, category) {
    const target = allFrames[targetRole] && allFrames[targetRole][targetKey];
    if (target) {
      connections.push({
        sourceId: sourceNode.id,
        sourceName: sourceName,
        triggerLabel: triggerLabel,
        targetId: target.id,
        targetName: target.name,
        category: `[${targetRole.toUpperCase()}] ${category}`
      });
    } else {
      const missingKey = `${targetRole.toUpperCase()}.${targetKey}`;
      if (!missing.includes(missingKey)) missing.push(missingKey);
    }
  }

  // --- Student Connections ---
  const st = allFrames.student;
  if (st.login && st.dashboard) {
    const btn = findChildByNameOrText(st.login.node, [/login/i, /sign\s*in/i]) || st.login.node;
    addConnection(btn, "Student Login", "On Click (Submit)", "student", "dashboard", "Auth Flow");
  }

  const studentScreensWithBottomNav = [
    st.dashboard, st.courses, st.analytics, st.notifications, st.profile,
    st.performanceDetails, st.prediction, st.gpa, st.report
  ].filter(Boolean);

  const studentBottomNavSpecs = [
    { name: "Home", key: "dashboard", patterns: [/nav_home/i, /^home$/i, /ic_home/i] },
    { name: "Courses", key: "courses", patterns: [/nav_courses/i, /^courses$/i, /my\s*courses/i] },
    { name: "Analytics", key: "analytics", patterns: [/nav_analytics/i, /^analytics$/i] },
    { name: "Notifications", key: "notifications", patterns: [/nav_notifications/i, /^notifications$/i] },
    { name: "Profile", key: "profile", patterns: [/nav_profile/i, /^profile$/i] }
  ];

  studentScreensWithBottomNav.forEach(screen => {
    studentBottomNavSpecs.forEach(spec => {
      const navBtn = findChildByNameOrText(screen.node, spec.patterns);
      if (navBtn) addConnection(navBtn, screen.name, `Bottom Nav (${spec.name})`, "student", spec.key, "Bottom Navigation");
    });
  });

  if (st.dashboard) {
    const qa = [
      { patterns: [/overall\s*performance/i, /performance\s*details/i], key: "performanceDetails", label: "Overall Performance" },
      { patterns: [/predicted\s*score/i, /prediction/i], key: "prediction", label: "Predicted Exam Score" },
      { patterns: [/attendance/i], key: "attendance", label: "Attendance" },
      { patterns: [/assignments?/i], key: "assignments", label: "Assignments" },
      { patterns: [/quizz?es?/i], key: "quizzes", label: "Quizzes" },
      { patterns: [/exams?/i], key: "exams", label: "Exams" },
      { patterns: [/gpa/i], key: "gpa", label: "GPA Progress" },
      { patterns: [/recommendations?/i], key: "recommendations", label: "Recommendations" }
    ];
    qa.forEach(item => {
      const btn = findChildByNameOrText(st.dashboard.node, item.patterns);
      if (btn) addConnection(btn, "Student Dashboard", item.label, "student", item.key, "Quick Actions");
    });
  }

  // --- Teacher Connections ---
  const tc = allFrames.teacher;
  if (tc.login && tc.dashboard) {
    const btn = findChildByNameOrText(tc.login.node, [/login/i, /sign\s*in/i]) || tc.login.node;
    addConnection(btn, "Teacher Login", "On Click (Submit)", "teacher", "dashboard", "Auth Flow");
  }

  // --- Admin Connections ---
  const ad = allFrames.admin;
  if (ad.login && ad.dashboard) {
    const btn = findChildByNameOrText(ad.login.node, [/login/i, /sign\s*in/i]) || ad.login.node;
    addConnection(btn, "Admin Login", "On Click (Submit)", "admin", "dashboard", "Auth Flow");
  }

  return { connections, missing };
}

figma.ui.onmessage = async (msg) => {
  if (msg.type === 'init' || msg.type === 'rescan') {
    const allFrames = findFrames();
    const { connections, missing } = generateConnections(allFrames);

    const summaryMap = {};
    for (const role of ['student', 'teacher', 'admin']) {
      summaryMap[role] = {};
      for (const [k, v] of Object.entries(allFrames[role])) {
        summaryMap[role][k] = { name: v.name, id: v.id };
      }
    }

    figma.ui.postMessage({
      type: 'scan-results',
      frames: summaryMap,
      connections: connections,
      missing: missing
    });
  }

  if (msg.type === 'apply-connections') {
    const selectedConnections = msg.connections || [];
    let count = 0;

    for (const conn of selectedConnections) {
      try {
        const sourceNode = figma.getNodeById(conn.sourceId);
        if (!sourceNode) continue;

        const actionObj = {
          type: "NODE",
          destinationId: conn.targetId,
          navigation: "NAVIGATE",
          transition: null,
          preserveScrollPosition: false
        };

        const reaction = { trigger: { type: "ON_CLICK" }, action: actionObj, actions: [actionObj] };

        if (typeof sourceNode.setReactionsAsync === "function") {
          await sourceNode.setReactionsAsync([reaction]);
        } else {
          sourceNode.reactions = [reaction];
        }
        count++;
      } catch (err) {
        console.error("Wiring error:", conn, err);
      }
    }

    figma.ui.postMessage({
      type: 'apply-success',
      count: count
    });
  }
};
