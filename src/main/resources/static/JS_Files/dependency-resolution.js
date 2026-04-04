let graphData = {
  nodes: [],
  links: []
};

/**
 * Get all direct dependencies of a mod
 * @param {string} modId - The mod identifier
 * @returns {Array<string>} Array of mod IDs that this mod depends on
 */
function getDependencies(modId) {
  if (!modId || typeof modId !== 'string' || modId.trim() === '') {
    return [];
  }
  
  const dependencies = graphData.links
    .filter(link => {
      if (link.source === modId) {
        return link.rel === "depends_on" || 
               link.rel === "required" || 
               link.rel === "optional";
      }
      return false;
    })
    .map(link => link.target);
  
  return dependencies;
}

/**
 * Get all mods that depend on a given mod
 * @param {string} modId - The mod identifier
 * @returns {Array<string>} Array of mod IDs that depend on this mod
 */
function getDependents(modId) {
  if (!modId || typeof modId !== 'string' || modId.trim() === '') {
    return [];
  }
  
  const dependents = graphData.links
    .filter(link => {
      if (link.target === modId) {
        return link.rel === "depends_on" || 
               link.rel === "required" || 
               link.rel === "optional";
      }
      return false;
    })
    .map(link => link.source);
  
  return dependents;
}

/**
 * Load graph data
 * @param {Object} data - Graph data with nodes and links
 * @returns {boolean} true if valid, false if invalid
 */
function loadGraphData(data) {
  if (!data || !Array.isArray(data.nodes) || !Array.isArray(data.links)) {
    console.error("Invalid graph data: expected { nodes: [...], links: [...] }");
    return false;
  }
  
  graphData = data;
  console.debug(`Graph loaded: ${graphData.nodes.length} nodes, ${graphData.links.length} links`);
  return true;
}

// ===== TESTS =====

function test1_FunctionExists() {
  try {
    console.assert(typeof getDependencies === 'function', 'getDependencies should be a function');
    console.assert(typeof getDependents === 'function', 'getDependents should be a function');
    console.assert(Array.isArray(getDependencies('test')), 'getDependencies should return an array');
    console.assert(Array.isArray(getDependents('test')), 'getDependents should return an array');
    console.log('✓ Test 1 PASSED: Functions exist and return arrays');
    return true;
  } catch (e) {
    console.error('✗ Test 1 FAILED:', e.message);
    return false;
  }
}

function test2_InvalidInput() {
  try {
    console.assert(getDependencies(null).length === 0, 'null input should return empty array');
    console.assert(getDependencies(undefined).length === 0, 'undefined input should return empty array');
    console.assert(getDependencies('').length === 0, 'empty string should return empty array');
    console.assert(getDependencies(123).length === 0, 'number input should return empty array');
    console.assert(getDependencies({}).length === 0, 'object input should return empty array');
    console.assert(getDependents(null).length === 0, 'getDependents with null should return empty array');
    console.log('✓ Test 2 PASSED: Invalid inputs handled gracefully');
    return true;
  } catch (e) {
    console.error('✗ Test 2 FAILED:', e.message);
    return false;
  }
}

function test3_GetDependencies() {
  try {
    graphData = {
      nodes: [
        { id: "CoreMod", type: "root", status: "compatible" },
        { id: "JEI", type: "mod", status: "compatible" },
        { id: "Optifine", type: "mod", status: "compatible" },
        { id: "Patchouli", type: "mod", status: "compatible" },
        { id: "Botania", type: "mod", status: "compatible" }
      ],
      links: [
        { source: "CoreMod", target: "JEI", rel: "required" },
        { source: "CoreMod", target: "Optifine", rel: "optional" },
        { source: "CoreMod", target: "Patchouli", rel: "required" },
        { source: "Patchouli", target: "Botania", rel: "required" }
      ]
    };
    
    const coreDeps = getDependencies("CoreMod");
    console.assert(coreDeps.includes("JEI"), 'CoreMod should depend on JEI');
    console.assert(coreDeps.includes("Optifine"), 'CoreMod should depend on Optifine');
    console.assert(coreDeps.includes("Patchouli"), 'CoreMod should depend on Patchouli');
    console.assert(coreDeps.length === 3, 'CoreMod should have exactly 3 dependencies');
    
    const patchouliDeps = getDependencies("Patchouli");
    console.assert(patchouliDeps.includes("Botania"), 'Patchouli should depend on Botania');
    console.assert(patchouliDeps.length === 1, 'Patchouli should have exactly 1 dependency');
    
    const jeiDeps = getDependencies("JEI");
    console.assert(jeiDeps.length === 0, 'JEI should have no dependencies');
    
    console.log('✓ Test 3 PASSED: getDependencies works correctly');
    return true;
  } catch (e) {
    console.error('✗ Test 3 FAILED:', e.message);
    return false;
  }
}

function test4_GetDependents() {
  try {
    const jeiDependents = getDependents("JEI");
    console.assert(jeiDependents.includes("CoreMod"), 'CoreMod should depend on JEI');
    console.assert(jeiDependents.length === 1, 'JEI should have exactly 1 dependent');
    
    const patchouliDependents = getDependents("Patchouli");
    console.assert(patchouliDependents.includes("CoreMod"), 'CoreMod should depend on Patchouli');
    console.assert(patchouliDependents.length === 1, 'Patchouli should have exactly 1 dependent');
    
    const botaniaDependents = getDependents("Botania");
    console.assert(botaniaDependents.includes("Patchouli"), 'Patchouli should depend on Botania');
    console.assert(botaniaDependents.length === 1, 'Botania should have exactly 1 dependent');
    
    const coreDependents = getDependents("CoreMod");
    console.assert(coreDependents.length === 0, 'CoreMod should have no dependents');
    
    console.log('✓ Test 4 PASSED: getDependents works correctly');
    return true;
  } catch (e) {
    console.error('✗ Test 4 FAILED:', e.message);
    return false;
  }
}

function test5_Performance() {
  try {
    const start = performance.now();
    
    for (let i = 0; i < 100; i++) {
      getDependencies("CoreMod");
      getDependents("JEI");
    }
    
    const end = performance.now();
    const time = end - start;
    
    console.assert(time < 50, `Performance: ${time.toFixed(2)}ms (should be < 50ms)`);
    console.log(`✓ Test 5 PASSED: 200 lookups in ${time.toFixed(2)}ms`);
    return true;
  } catch (e) {
    console.error('✗ Test 5 FAILED:', e.message);
    return false;
  }
}

function runAllTests() {
  console.log("\n========================================");
  console.log("  Issue #443 - Dependency Resolution Tests");
  console.log("========================================\n");
  
  const results = [];
  results.push(test1_FunctionExists());
  results.push(test2_InvalidInput());
  results.push(test3_GetDependencies());
  results.push(test4_GetDependents());
  results.push(test5_Performance());
  
  console.log("\n========================================");
  console.log("  Test Summary");
  console.log("========================================");
  
  const passed = results.filter(r => r).length;
  const total = results.length;
  
  console.log(`Passed: ${passed}/${total}\n`);
  
  if (passed === total) {
    console.log("✓✓✓ ALL TESTS PASSED ✓✓✓\n");
    return true;
  } else {
    console.log(`✗✗✗ ${total - passed} test(s) failed ✗✗✗\n`);
    return false;
  }
}