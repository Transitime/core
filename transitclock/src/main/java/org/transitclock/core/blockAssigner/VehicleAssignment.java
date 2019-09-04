package org.transitclock.core.blockAssigner;

/**
 * model class representing an external vehicle block assignment.
 */
public class VehicleAssignment {
    private String _vehicleId;
    private String _blockId;

    public VehicleAssignment(String vehicleId, String blockId) {
        this._vehicleId = vehicleId;
        this._blockId = blockId;
    }

    public String getVehicleId() {
        return _vehicleId;
    }

    public String getBlockId() {
        return _blockId;
    }
}

