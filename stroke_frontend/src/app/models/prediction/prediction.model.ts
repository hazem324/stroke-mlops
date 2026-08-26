export interface Prediction {
  id: number;

  predictionFile: string;
  previewFile: string;
  overlayFile: string;

  predictionShapeX: number;
  predictionShapeY: number;
  predictionShapeZ: number;

  previewSlice: number;

  lesionDetected: boolean;
  lesionVoxels: number;
  lesionVolumeMm3: number;

  centroidIndexX: number;
  centroidIndexY: number;
  centroidIndexZ: number;

  centroidPhysicalX: number;
  centroidPhysicalY: number;
  centroidPhysicalZ: number;

  boundingBoxMinX: number;
  boundingBoxMaxX: number;

  boundingBoxMinY: number;
  boundingBoxMaxY: number;

  boundingBoxMinZ: number;
  boundingBoxMaxZ: number;

  processingTime: number;
  createdAt: string;
}