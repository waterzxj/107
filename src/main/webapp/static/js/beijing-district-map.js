(function(window) {
  if (!window.global) {
    window.global = {};
  }
  var district = window.global.district = {};
  var beijing = district.beijing = {
    "extra": {
      "text": [{
        content: "1 西城",
        position: [285, 215]
      }, {
        content: "2 东城",
        position: [285, 235]
      }, {
        content: "3 石景山",
        position: [285, 255]
      }]
    },
    "延庆": {
      districtFill: "#7ee5d8",
      position: ["87", "55"],
      districtD: "M37.881,104.018c0.657-2.995-0.325-6.289,0.636-9.213c2.69-8.185,11.224-0.295,15.944-2.725c4.697-2.417,8.583-3.153,13.955-2.687c1.641,0.142,3.551,0.941,4.67,2.188c0.495,0.55,0.883,0.938,1.186,1.63c0.258,0.591,3.162,7.748,4.647,1.126c0.965-4.3-1.553-8.761,0.11-12.936c1.303-3.27,5.687-5.55,8.833-6.51c5.722-1.747,11.359,0.735,17.183,0.265c5.874-0.475,16.369,0.195,16.529-7.796c0.126-6.303,2.259-12.2,2.172-18.467c-0.071-5.187-1.064-10.809-0.638-15.95c0.444-5.352,14.601-8.664,18.667-6.996c3.835,1.573,3.333,6.034,6.848,7.866c5.865,3.055,12.739,1.681,18.861,0.185c5.472-1.337,10.022-0.442,15.567-0.593c7.467-0.204,6.479-7.4,1.057-8.956c-1.825-0.523-3.715-0.714-5.554-1.175c-2.242-0.562-6.299-1.82-5.953-4.939c0.397-3.582,4.11-4.983,3.97-8.182c-66.166,2.63-122.685,42.738-148.915,99.687C32.077,109.563,36.908,108.459,37.881,104.018z",
      text: "延庆"
    },
    "密云": {
      districtFill: "#7ee5d8",
      position: ["255", "80"],
      districtD: "M307.538,127.413c0.092,0.068,0.173,0.137,0.261,0.205c1.629,0.313,3.276,0.315,4.871-0.267c2.816-1.029,8.104-4.786,5.22-8.535c-0.678-0.881-1.324-1.732-1.897-2.685c-0.403-0.672-0.789-2.292-0.244-3.002c0.98-1.281,1.896-2.083,3.604-2.25c3.897-0.382,6.572-1.9,8.421-5.428c1.662-3.176,2.813-6.564,4.179-9.862c-19.476-33.572-50.006-59.933-86.615-74.091c-0.624,1.802-1.229,3.608-1.684,5.454c-1.434,5.829-0.484,9.97,1.848,15.379c2.225,5.16,5.616,11.676,4.896,17.476c-0.306,2.47-1.095,4.699-1.738,7.092c-0.984,3.65-0.387,6.603,0.938,10.041c1.358,3.525,2.771,7.079,4.709,10.332c1.275,2.141,2.896,4.456,3.463,6.931c0.992,4.346-0.498,7.353-2.934,10.913c-1.375,2.011-2.73,4.377-4.225,6.679c2.82,2.023,6.793,0.972,9.982,0.6c3.456-0.403,5.2-2.103,8.077-3.585c2.091-1.077,4.321-1.768,6.683-1.893c3.078-0.163,3.922-2.387,6.141-3.686c2.089-1.223,4.3,1.723,4.367,3.563c0.104,2.838-0.194,4.809,2.72,6.579c0.724,0.439,1.69,0.912,2.269,1.546c1.16,1.274,1.652,2.232,1.735,4.005c0.206,4.375,5.108,4.53,8.134,5.976c1.786,0.854,3.785,1.844,5.846,2.428C306.838,127.165,307.186,127.152,307.538,127.413z",
      text: "密云"
    },
    "怀柔": {
      districtFill: "#7ee5d8",
      position: ["180", "80"],
      districtD: "M127.829,68.68c3.022-0.414,7.324-1.254,9.232,1.963c1.121,1.889,1.418,5.48,3.075,6.829c2.246,1.829,4.832,2.085,6.951,4.108c0.33,0.314,0.422,1.04,0.398,1.445c-0.066,1.146,1.875,1.938,2.719,2.388c2.364,1.262,3.779,3.643,6.513,4.552c2.809,0.934,8.462,1.203,10.197,4.028c1.876,3.054-1.431,6.409-0.221,9.724c1.407,3.854,6.587,8.561,10.262,10.294c3.855,1.818,9.008,3.709,13.268,2.266c2.392-0.81,4.598-2.026,7.08-2.538c4.896-1.009,8.479,3.488,12.901,4.373c5.344,1.069,9.847-3.618,15.081-1.884c5.229,1.733,11.539,5.169,16.904,1.771c4.742-3.003,7.742-9.314,10.803-13.96c1.824-2.77,3.971-5.762,2.718-9.271c-1.149-3.224-3.237-5.816-4.631-8.896c-1.966-4.346-5.62-10.336-5.233-15.253c0.191-2.436,0.922-4.603,1.59-6.941c1.135-3.973,0.842-7.579-0.406-11.509c-1.881-5.926-6.553-12.501-6.402-18.898c0.104-4.369,1.411-8.436,2.771-12.509C224.75,13.809,204.57,10,183.5,10c-1.581,0-3.157,0.025-4.728,0.067c0.185,2.293-1.164,3.703-3.108,6.118c-3.739,4.645,5.35,5.338,7.416,5.803c1.673,0.376,3.148,0.932,4.482,2.063c2.383,2.026,5.354,6.406,2.084,9.172c-4.9,4.146-10.738,1.126-16.486,1.722c-7.819,0.81-21.23,7.003-27.661-0.84c-1.172-1.43-1.505-3.057-2.466-4.575c-1.944-3.069-6.915-1.642-9.673-0.785c-2.702,0.838-5.182,1.838-7.436,3.562c-1.379,1.055-0.488,4.62-0.358,6.192c0.483,5.854,1.08,12.623-0.512,18.346c-0.887,3.19-1.258,6.051-1.325,9.369c-0.021,1.026-0.072,2.015-0.21,2.992C124.961,69.092,126.393,68.876,127.829,68.68z",
      text: "怀柔"
    },
    "平谷": {
      districtFill: "#7ee5d8",
      position: ["320", "160"],
      districtD: "M312.414,137.385c1.151,5.462,3.412,10.525,5.125,15.816c2.803,8.657,7.337,20.372,4.192,29.471c-1.135,3.281-4.173,8.438-2.725,11.958c2.188,5.317,10.149,6.005,15.068,5.567c5.733-0.512,6.849-4.584,11.206-7.104c2.794-1.615,6.408-1.437,9.465-2.298c0.164-3.078,0.253-6.177,0.253-9.297c0-30.285-7.856-58.731-21.634-83.424c-1.415,3.817-2.748,7.713-5.284,10.772c-1.334,1.609-3.139,3.589-5.362,3.855c-2.09,0.25-5.764,1.431-3.663,4.161c0.468,0.607,1.068,1.258,1.332,1.994c1.475,4.129-2.126,8.712-5.91,10.095c-1.469,0.536-2.979,0.859-4.5,0.917C311.432,131.964,311.771,134.34,312.414,137.385z",
      text: "平谷"
    },
    "昌平": {
      districtFill: "#EBAC40",
      position: ["75", "130"],
      districtD: "M25.412,166.551c1.459,1.257,3.203,2.894,3.394,4.96c0.095,1.024,0.034,1.735-0.317,2.716c-1.162,3.248,4.917,4.249,6.696,4.312c6.328,0.223,13.064,2.423,19.319,1.884c4.83-0.417,4.735-4.461,7.599-7.097c1.811-1.667,5.069-2.122,5.87-4.609c0.206-0.641,0.958-1.01,1.566-0.637c2.109,1.292,3.457-0.734,5.179-1.441c2.479-1.017,5.617,0.879,8.24,0.031c2.458-0.795,1.389-5.822,3.839-7.257c1.937-1.134,8.024-5.395,11.012-3.135c3.291,2.489,8.341,3.889,10.738,7.318c0.536,0.767,1.124,2.889,1.188,3.737c0.218,2.914,1.73,3.273,3.806,4.413c1.248,0.686,1.928,2.571,2.434,3.805c1.154,2.816,3.912,4.419,5.782,6.695c0.6,0.729,0.806,1.668,1.244,2.482c1.578,2.936,4.311,2.16,6.701,2.404c1.284,0.133,1.91,0.108,2.913,0.99c4.574,4.02,7.883-2.795,12.731-1.893c3.389,0.631,3.621,11.696,8.403,8.182c2.695-1.982,5.041-6.582,8.637-7.107c3.343-0.488,6.575,0.117,7.909-4.068c0.491-1.539,1.104-2.456,2.191-3.649c0.612-0.671,1.626-0.985,2.359-1.478c2.51-1.686,2.653-4.728,2.865-7.331c0.193-2.388,0.415-4.727,0.904-7.074c0.673-3.225,1.587-6.437,0.773-9.743c-0.506-2.054-1.057-4.007-0.822-6.147c0.371-3.38,1.135-6.789-0.005-10.124c-0.541-1.582-1.44-3.255-1.614-4.934c-0.201-1.939,0-4.035,0.813-5.839c1.186-2.632,3.786-5.474,2.882-8.635c-0.041-0.144-0.05-0.276-0.039-0.4c-1.737-0.564-3.373-1.289-4.721-2.032c-5.812-3.201-13.21-9.769-11.142-17.1c1.738-6.161-6.729-5.379-10.683-7.637c-2.245-1.282-4.307-3.858-6.747-4.593c-1.353-0.408-1.573-0.949-1.979-2.152c-0.078-0.445-0.07-0.893,0.024-1.345c-1.141-1.143-2.466-1.93-3.976-2.362c-0.63-0.229-1.276-0.484-1.798-0.909c-1.42-1.156-2.279-2.438-2.818-4.188c-2.266-7.355-8.1-4.554-13.744-4.188c-0.013,0.038-0.019,0.076-0.032,0.114c-1.386,3.949-6.585,4.484-10.048,5.005c-5.186,0.78-10.392,1.208-15.581,0.365c-6.477-1.053-17.764,0.485-16.656,9.498c0.41,3.332,1.47,7.288-0.634,10.248c-1.508,2.122-4.778,2.226-6.366,0.298c-2.321-2.818-1.011-5.453-6.726-5.506c-3.218-0.03-6.353-0.135-9.281,1.401c-3.401,1.784-5.59,3.095-9.007,1.28c-3.09-1.641-8.07-1.946-8.329,2.579c-0.1,1.746,0.05,3.498-0.061,5.24c-0.492,7.736-7.085,9.632-13.598,10.052c-6.708,15.101-11.29,31.353-13.389,48.374C17.662,161.688,22.118,163.712,25.412,166.551z",
      text: "昌平"
    },
    "顺义": {
      districtFill: "#EBAC40",
      position: ["225", "160"],
      districtD: "M262.831,203.363c5.39,0.084,10.696-0.578,14.748-4.45c3.428-3.274,5.044-5.302,10.037-4.606c0.627,0.086,1.22,0.856,1.639,1.238c2.614,2.389,4.362-0.24,6.17-1.23c1.783-0.976,3.746-1.154,5.752-1.385c5.002-0.572,10.573-1.184,15.513,0.221c-0.091-3.055,1.128-5.928,2.201-8.844c2.266-6.145,1.562-11.709,0.096-18.063c-1.492-6.467-3.491-12.772-5.814-18.986c-1.128-3.016-2.299-6.112-2.813-9.306c-0.529-3.297-0.643-6.03-3.469-8.358c-0.482-0.104-0.965-0.228-1.442-0.396c-2.944-1.031-6.164-3.436-9.194-3.904c-2.552-0.395-4.279-0.844-5.395-3.322c-0.422-0.935-0.305-2.06-0.407-3.05c-0.257-2.479-2.29-3.283-3.966-4.373c-1.493-0.973-2.615-2.751-2.68-4.547c-0.041-1.171-0.566-6.002-2.35-3.476c-0.294,0.417-0.627,0.829-1.047,1.121c-2.605,1.81-5.732,0.985-8.596,2.078c-2.572,0.981-4.555,3.11-7.149,3.955c-4.267,1.388-11.334,2.769-15.203-0.094c-0.015-0.011-0.024-0.024-0.039-0.035c-3.151,4.479-7.018,8.261-12.854,7.958c-4.984-0.258-11.233-4.884-15.883-3.24c-4.053,1.434-8.186,3.267-12.426,1.418c-3.459-1.508-7.58-4.989-11.535-3.658c-2.531,0.851-4.83,2.036-7.44,2.664c-1.938,0.466-4.203,0.305-6.435-0.196c0.86,5.421-5.577,9.399-3.53,15.052c0.823,2.272,1.918,4.606,1.966,7.07c0.056,2.84-0.853,5.762-0.642,8.568c0.252,3.364,1.403,6.234,1.088,9.645c-0.219,2.391-0.961,4.754-1.371,7.117c-0.677,3.905-0.145,8.516-2.295,11.95c2.52,1.813,3.457,4.853,6.305,6.374c3.026,1.615,5.937,2.598,9.282,3.268c5.716,1.145,8.08,4.402,11.199,9.004c4.856,7.166,15.039,5.486,22.758,6.23c8.478,0.816,16.902,0.049,25.39,0.16c2.869,0.037,5.946,0.32,9.015,0.407C262.279,203.236,262.575,203.23,262.831,203.363z",
      text: "顺义"
    },
    "门头沟": {
      districtFill: "#EBAC40",
      position: ["18", "225"],
      districtD: "M57.344,266.98c3.401-0.067,1.762-5.61,3.403-8.041c1.338-1.98,3.027-2.98,5.414-3.326c6.421-0.93,12.624-1.393,18.979-0.786c-6.396-4.864-10.351-12.324-13.528-19.556c-1.102-2.51-3.04-5.943-1.188-8.479c2.626-3.597-2.226-6.228,0.145-9.918c3.37-5.248,11.397-3.173,16.929-3.462c-0.5-1.116-0.563-2.706-0.546-3.819c0.059-3.965-1.79-4.896-5.48-6.032c-5.843-1.801-11.75-2.709-17.611-4.25c-6.981-1.836-7.667-10.795-7.059-17.088c-5.168,1.938-12.444-1.289-17.587-1.462c-2.67-0.09-14.797-0.359-13.196-5.936c1.859-6.473-6.522-10.375-12.962-12.374C12.363,168.707,12,175.061,12,181.5c0,25.088,5.389,48.915,15.069,70.393c3.999,0.693,7.675,2.539,11.469,4.889c2.685,1.662,5.52,3.068,8.182,4.758c2.778,1.763,5.657,4.038,8.802,5.102c0.486,0.164,0.895,0.246,1.272,0.293C56.965,266.893,57.151,266.9,57.344,266.98z",
      text: "门头沟"
    },
    "房山": {
      districtFill: "#EBAC40",
      position: ["60", "300"],
      districtD: "M100.29,326.561c3.26-6.129,7.501-11.551,8.527-18.535c0.58-3.949,3.094-9.164,2.074-13.119c-0.672-2.604-2.925-3.891-3.585-6.289c-0.287-1.044,0.187-1.623,0.42-2.566c0.369-1.496-0.455-3.275-0.925-4.65c-0.804-2.354-0.77-5.916-2.255-7.945c-2.653-3.624-5.157,4.639-5.392,5.622c-0.389,1.627-0.676,3.348-1.269,4.917c-0.715,1.895-1.628,3.433-3.245,4.693c-3.264,2.545-10.782,0.125-14.155-0.338c-3.783-0.521-12.192-0.805-14.042-5.15c-0.697-1.639-0.489-3.567-1.137-5.247c-1.549-4.015-4.876-6.444-8.343-8.702c-0.066-0.014-0.128-0.02-0.196-0.034c-10.4-2.415-18.365-12.586-28.62-15.001c14.979,31.948,39.52,58.521,69.937,76.029C98.86,329.102,99.59,327.875,100.29,326.561z",
      text: "房山"
    },
    "大兴": {
      districtFill: "#EBAC40",
      position: ["130", "320"],
      districtD: "M234.793,332.479c-4.471-1.354-8.085-3.342-10.463-7.524c-1.502-2.642-3.334-5.209-4.727-7.886c-0.488-0.941-3.511-7.055-5.388-2.771c-0.354,0.809-0.672,1.53-1.163,2.266c-2.039,3.045-8.062,2.662-11.074,2.373c-6.922-0.662-15.535-1.015-13.988-10.219c1.017-6.046,2.74-10.984,1.813-17.255c-0.393-2.657-0.619-5.177-0.314-7.862c0.376-3.307,1.33-6.512-0.028-9.625c-1.603,1.515-2.976,3.236-5.42,3.786c-2.974,0.669-5.933,0.75-8.971,0.519c-3.107-0.236-9.032-0.223-11.649-2.939c-1.218,0.913-2.396,1.6-4.006,1.694c-2.001,0.118-10.008,2.062-5.674,3.433c2.493,0.79,6.471,1.626,8.171,3.786c1.256,1.597,1.338,3.364-0.448,4.716c-1.377,1.043-2.359,1.577-4.127,1.381c-3.001-0.334-3.556,2.488-4.749,4.522c-0.875,1.491-3.409,2.459-4.751,1.117c-0.708-0.708-0.891-1.974-1.168-2.867c-0.695-2.239-1.927-3.804-3.503-5.453c-3.115-3.26-8.226-7.748-11.202-1.63c-1.478,3.038-2.105,6.328-2.934,9.578c-0.198,0.777-1.225,1.035-1.781,0.471c-1.938-1.968-2.975-4.996-5.08-6.722c-3.198-2.621-8.312-0.448-12.096-1.334c0.605,2.126,2.518,3.799,2.875,6.308c0.499,3.501-0.536,6.215-1.251,9.611c-0.798,3.79-1.005,7.667-2.432,11.296c-1.282,3.26-3.831,6.163-5.481,9.276c-1.208,2.275-2.363,4.635-3.774,6.807C124.715,345.13,153.187,353,183.5,353c20.354,0,39.871-3.561,57.982-10.07C240.068,338.688,239.594,333.934,234.793,332.479z",
      text: "大兴"
    },
    "通州": {
      districtFill: "#EBAC40",
      position: ["235", "275"],
      districtD: "M307.96,281.286c-0.446-2.324-4.599-17.479-6.724-14.192c-4.314,6.676-15.245-0.754-18.973-4.326c-2.938-2.814-4.312-6.134-9.104-6.403c-1.563-0.089-3.073-1.13-3.979-2.367c-4.076-5.581-1.259-12.007-3.211-18.046c-1.42-4.391-6.984-7.491-5.044-12.733c1.089-2.938,3.075-5.62,3.007-8.884c-0.074-3.38-0.752-5.926-2.215-8.848c-5.363-0.021-10.762-0.438-16.111-0.289c-8.789,0.244-17.307-0.766-26.041-0.982c-1.531-0.037-3.059-0.064-4.551-0.171c4.135,5.593,6.864,12.294,7.027,19.322c0.174,7.476-1.779,12.887-5.227,19.327c-2.928,5.472-0.709,8.774,0.658,13.963c0.449,1.709,1.119,4.264,0.125,5.955c-1.099,1.869-3.119,2.695-4.125,4.705c-0.629,1.25-1.492,3.976-2.938,4.604c-5.89,2.561-12.017-2.279-17.956-0.047c-0.521,0.195-0.989,0.448-1.425,0.73c2.481,4.979-0.284,10.039,0.313,15.318c0.61,5.381,1.262,9.985-0.06,15.305c-1.517,6.103-3.229,12.265,5.255,13.041c2.704,0.246,5.451,0.741,8.172,0.681c4.039-0.093,5.854-1.343,7.999-4.382c5.13-7.269,12.539,10.367,14.024,12.414c2.551,3.514,5.574,4.283,9.358,5.721c1.189,0.451,2.54,0.967,3.373,1.99c2.271,2.789,2.658,6.315,3.817,9.548c27.229-10.158,51.181-27.038,69.842-48.62C310.887,289.771,308.815,285.739,307.96,281.286z",
      text: "通州"
    },
    "海淀": {
      districtFill: "#00ac97",
      position: ["85", "200"],
      districtD: "M59.028,181.283c-0.632,4.636-0.811,14.713,5.398,15.972c4.849,0.983,9.592,1.755,14.338,3.236c3.309,1.032,7.821,1.503,9.799,4.722c1.875,3.051-0.782,6.321,2.375,9.28c3.156,2.958,4.923,8.231,9.026,9.468c1.855,0.559,3.181,2.377,2.646,4.321c-0.457,1.661-2.663,3.063-2.917,4.668c-0.563,3.549,0.95,5.258,3.893,6.565c2.714,1.207,5.278,2.444,6.759,5.201c3.003,5.592,15.356,3.844,20.618,2.385c4.655-1.293-0.452-8.449,0.476-11.936c1.034-3.883,3.633-6.67,6.6-9.287c4.495-3.968,12.125-4.931,8.102-12.639c-1.805-3.457-3.927-6.906-4.908-10.704c-0.54-2.091,0.167-5.556,2.427-6.519c1.628-0.692,4.095-0.078,6.251,0.099c-0.688-0.472-1.361-1.126-2.039-1.95c-1.313-1.6-1.645-2.879-2.249-4.727c-1.158-3.537-6.232,1.465-7.686,2.211c-1.256,0.646-3.366,0.584-4.563-0.2c-2.264-1.483-3.813-2.021-6.551-2.292c-5.564-0.551-4.895-4.434-8.674-7.693c-1.938-1.672-3.272-2.983-4.234-5.348c-1.654-4.066-4.191-1.928-5.503-4.58c-0.922-1.866-0.937-3.837-1.435-5.882c-0.826-3.392-5.719-3.938-8.183-5.735c-4.088-2.983-5.432-1.417-9.501,0.663c-1.622,0.829-1.976,0.539-2.478,2.623c-0.27,1.12-0.14,2.484-0.646,3.539c-1.2,2.499-4.895,2.509-7.235,2.184c-4.259-0.592-5.939,2.881-9.435,1.504c-1.417,2.259-4.171,2.202-5.89,4.4c-1.699,2.172-2.593,4.491-4.602,6.121C59.024,181.061,59.044,181.165,59.028,181.283z",
      text: "海淀"
    },
    "石景山": {
      districtFill: "#00ac97",
      position: ["79", "235"],
      districtD: "M88.944,215.395c-3.843,0.282-21.021-1.068-16.224,6.047c1.367,2.026,1.864,3.58,0.283,5.674c-2.349,3.107,0.344,7.035,1.87,10.212c2.872,5.976,7.789,15.97,14.88,17.761c7.568,1.911,13.247-4.72,19.795-7.354c-1.073-1.692-1.968-3.897-3.841-4.849c-1.816-0.922-4.114-1.571-5.734-2.819c-2.258-1.736-3.532-4.83-2.334-7.683c0.732-1.741,4.922-5.591,0.798-6.657c-1.829-0.473-2.84-0.897-4.073-2.334c-1.424-1.662-1.219-3.912-2.969-5.553C90.53,217.029,89.702,216.249,88.944,215.395z",
      text: "3"
    },
    "丰台": {
      districtFill: "#00ac97",
      position: ["95", "270"],
      districtD: "M161.083,263.898c-0.259,0.01-0.453-0.074-0.615-0.192c-2.375-0.123-4.792-0.529-7.161-0.397c-2.661,0.146-4.919,1.004-7.503,1.387c-2.663,0.394-5.663,0.561-7.988-1.109c-4.754-3.413-5.273-9.036-6.007-14.432c-1.349,0.384-2.831,0.51-4.062,0.645c-3.5,0.385-12.295,1.983-16.481-0.432c-6.836,2.186-14.352,10.627-22.111,7.834c-0.212,0.134-0.468,0.21-0.771,0.16c-5.552-0.916-10.489-0.938-16.05-0.295c-1.849,0.213-3.785,0.222-5.605,0.604c-3.751,0.786-4.408,3.068-4.625,6.244c-0.133,1.943-0.764,3.882-2.131,4.835c4.307,3.147,7.227,6.635,8.17,12.407c0.648,3.968,7.941,4.455,11.183,4.899c3.675,0.505,8.558,1.909,12.257,1.493c6.567-0.736,4.541-13.1,9.953-16.471c3.777-2.354,5.6,2.506,6.174,5.27c0.528,2.539,2.923,6.815,2.318,9.27c-0.036,0.146-0.055,0.285-0.077,0.426c0.192-0.085,0.409-0.13,0.666-0.068c4.131,0.979,8.034-1.1,11.952,0.988c2.186,1.165,3.435,3.84,4.891,5.984c1.061-4.76,2.726-12.01,7.952-12.229c5.782-0.243,11.838,6.657,13.312,11.838c1.145,4.024,2.696-0.935,3.442-2.524c0.376-0.801,1.849-1.885,2.776-1.914c0.231-0.008,8.489,0.005,5.462-2.355c-1.258-0.98-2.979-1.89-4.498-2.381c-1.258-0.406-2.632-0.688-3.821-1.275c-1.804-0.894-2.51-2.063-1.576-3.926c2.246-4.482,8.479-2.074,11.738-4.599c0.027-0.021,0.056-0.034,0.083-0.054c-0.542-1.439-0.654-2.767-0.023-4.274c0.28-0.671,0.851-1.245,1.408-1.694c0.976-0.789,0.89-2.087,0.187-3.053C163.143,263.463,162.21,263.861,161.083,263.898z",
      text: "丰台"
    },
    "朝阳": {
      districtFill: "#00ac97",
      position: ["170", "225"],
      districtD: "M148.975,220.336c2.507,0.063,4.905-0.041,7.154-1.589c0.37-0.255,0.999-0.174,1.292,0.166c4.438,5.155,6.388,12.002,5.918,18.776c-0.285,4.1-0.86,7.666,0.013,11.754c0.606,2.838,0.329,5.736,0.346,8.611c0.002,0.465-0.175,1.627,0.06,2.021c0.192,0.321,0.745,0.444,1.05,0.788c0.622,0.701,0.705,1.742,1.083,2.568c0.217,0.473,0.345,0.978,0.527,1.463c0.174,0.463,0.688,1.051,0.787,1.483c0.23,1.001-1.265,2.028-1.981,2.685c-4.134,3.777,2.251,6.256,5.129,6.646c3.383,0.46,7.17,0.921,10.574,0.464c2.435-0.325,4.737-0.942,6.345-2.93c0.566-0.7,1.183-1.616,1.972-2.093c3.68-2.228,7.837-2.195,11.95-1.324c2.274,0.482,7.322,2.132,8.869-0.605c0.556-0.98,1.062-1.979,1.574-2.982c0.75-1.463,1.555-2.413,2.907-3.416c3.702-2.741-0.942-9.051-1.364-12.133c-0.32-2.342-0.23-4.127,0.512-6.397c0.881-2.69,2.904-4.831,4.02-7.405c1.59-3.673,2.335-7.916,2.242-11.91c-0.178-7.617-2.572-14.4-7.204-20.438c-0.205-0.268-0.247-0.533-0.185-0.766c-3-0.477-5.775-1.539-8.041-4.066c-1.896-2.116-2.881-4.969-4.942-6.952c-3.233-3.112-6.642-2.651-10.529-4.06c-2.295-0.83-4.764-1.766-6.762-3.188c-2.277-1.619-3.224-4.402-5.493-5.921c-0.031-0.021-0.054-0.044-0.081-0.066c-1.709,1.417-3.548,0.833-4.367,4.284c-0.191,0.809-0.338,2.043-0.801,2.76c-0.951,1.479-1.898,2.058-3.609,2.551c-2.702,0.779-5.412-0.735-7.604,1.873c-1.387,1.65-2.932,3.369-4.676,4.65c-0.105,0.078-0.208,0.144-0.312,0.213c0.056,0.295-0.006,0.617-0.259,0.902c-2.954,3.327-5.432,0.268-9.003,0.734c-7.388,0.966,2.257,14.305,3.094,17.453C149.765,217.15,149.726,218.873,148.975,220.336z",
      text: "朝阳"
    },
    "西城": {
      districtFill: "#00ac97",
      position: ["135", "245"],
      districtD: "M134.42,247.754c-0.185,0.188-0.392,0.352-0.608,0.502c0.633,5.012,1.1,13.453,7.138,14.385c2.965,0.457,5.355-0.412,7.949-0.972c-0.51,0.133-1.095-0.06-1.26-0.719c-1.661-6.64,2.376-13.263,0.784-19.83c-0.647-2.667-2.036-5.353-2.155-8.12c-0.148-3.463,0.413-6.865,0.614-10.311c-0.292,0.225-0.6,0.446-0.945,0.664c-4.342,2.735-13.28,7.271-12.538,13.646C133.742,239.938,137.158,244.982,134.42,247.754z",
      text: "1"
    },
    "东城": {
      districtFill: "#00ac97",
      position: ["150", "245"],
      districtD: "M150.694,242.05c0.891,6.158-2.533,12.205-1,18.334c0.166,0.662-0.259,1.134-0.768,1.279c0.879-0.188,1.783-0.343,2.74-0.396c3.133-0.175,6.207,0.233,9.333,0.334c0.18,0.006,0.333,0.051,0.466,0.117c0.068-0.014,0.135-0.033,0.202-0.052c-0.038-0.108-0.07-0.222-0.068-0.335c0.073-4.896-0.263-9.342-0.826-14.195c-0.43-3.707,0.548-7.261,0.469-10.959c-0.025-1.17-2.48-16.664-5.24-14.765c-1.849,1.272-4.576,1.228-6.994,1.101c-0.269,3.748-1.365,7.957-0.432,11.576C149.27,236.773,150.295,239.293,150.694,242.05z",
      text: "2"
    }
  };
})(window);